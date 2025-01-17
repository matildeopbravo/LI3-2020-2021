#include "model/state.h"

#include <glib.h>
#include <stdlib.h>
#include "view/colors.h"

struct variable {
  VariableType type;
  int references;
  char *name;
  VariableValue value;
};

struct function {
  VariableType return_type;
  const char *help_text;

  int n_args;
  int defaultable;
  VariableType *args;

  FunctionPtr function;
};

Variable init_var(VariableType type, VariableValue val, const char *name) {
  Variable var = malloc(sizeof(struct variable));
  var->type = type;
  if (name != NULL)
    var->name = g_strdup(name);
  else
    var->name = NULL;
  var->value = val;
  var->references = 0;

  return var;
}

void free_var(Variable var) {
  if (var->name) {
    free(var->name);
  }

  switch (var->type) {
  case VAR_STRING:
    free(var->value.string);
    free(var);
    break;
  case VAR_SGR:
    free_sgr(var->value.sgr);
    free(var);
    break;
  case VAR_TABLE:
    free_table(var->value.table);
    free(var);
    break;
  case VAR_FUNCTION:
    free(var->value.function);
    free(var);
    break;
  default:
    free(var);
    break;
  }
}

void free_if_possible(Variable var) {
  if (var->references == 0)
    free_var(var);
}

VariableType get_var_type(Variable var) { return var->type; }

VariableValue get_var_value(Variable var) { return var->value; }

void set_var_name(Variable var, const char *name) {
  if (var != NULL)
    var->name = g_strdup(name);
}

void set_var_value(Variable var, VariableValue v) { var->value = v; }

void set_var_type(Variable var, VariableType t) { var->type = t; }

Variable void_var() {
  VariableValue val;
  val.number = 0;
  return init_var(VAR_VOID, val, NULL);
}

const char *type_name(VariableType type) {
  switch (type) {
  case VAR_NUMBER:
    return "number";
  case VAR_FLOAT:
    return "float";
  case VAR_SGR:
    return "SGR";
  case VAR_TABLE:
    return "table";
  case VAR_STRING:
    return "string";
  case VAR_FUNCTION:
    return "function";
  case VAR_OPERATOR:
    return "operator";
  case VAR_VOID:
    return "void";
  case VAR_ARRAY:
    return "array";
  case VAR_ANY:
    return "any";
  }
  return NULL;
}

STATE init_state() {
  STATE s = g_tree_new((GCompareFunc)g_strcmp0);

  // Vamos criar variáveis globais para os comparators
  VariableValue val;
  val.operator= GT;
  create_variable(s, init_var(VAR_OPERATOR, val, "GT"));
  val.operator= EQ;
  create_variable(s, init_var(VAR_OPERATOR, val, "EQ"));
  val.operator= LT;
  create_variable(s, init_var(VAR_OPERATOR, val, "LT"));

  return s;
}

void free_state(STATE s) {
  for (GTreeNode *n = g_tree_node_first(s); n != NULL;
       n = g_tree_node_next(n)) {
    free(g_tree_node_key(n));
    free_var(g_tree_node_value(n));
  }
  g_tree_destroy(s);
}

void create_variable(STATE state, Variable var) {
  Variable v = find_variable(state, var->name);
  if (v != NULL && v != var) {
    // Não entendo o que se passa, isto está a fazer com que não seja possivel redefinir variáveis, aleatoriamente crasha....
    v->references -= 1;
    free_if_possible(v);
    /* fprintf(stderr, BOLD FG_RED "Error: " RESET_ALL "cannot redefine variables.\n"); */
    /* return; */
  }

  var->references += 1;
  g_tree_replace_node(state, g_strdup(var->name), var);
}

Variable find_variable(STATE state, const char *name) {
  return g_tree_lookup(state, name);
}

TABLE state_table(STATE s) {
  char *header[2] = {"Variable", "Type"};
  TABLE t = new_table(header, 2);

  for (GTreeNode *n = g_tree_node_first(s); n != NULL;
       n = g_tree_node_next(n)) {
    add_field(t, g_tree_node_key(n));
    add_field(t, type_name(get_var_type(g_tree_node_value(n))));
  }

  return t;
}

FunctionVal create_function(int n_args, int defaultable,
                            VariableType return_type, FunctionPtr function,
                            const VariableType *args, const char *help) {
  FunctionVal ret = malloc(sizeof(struct function));

  ret->return_type = return_type;
  ret->help_text = help;
  ret->args = memcpy(malloc(n_args * sizeof(VariableType)), args,
                     n_args * sizeof(VariableType));
  ret->defaultable = defaultable;
  ret->n_args = n_args;
  ret->function = function;

  return ret;
}

int get_n_args(FunctionVal func) { return func->n_args; }
const char *get_function_help(FunctionVal func) { return func->help_text; }

int get_defaultable(FunctionVal func) { return func->defaultable; }

VariableType get_arg_type(FunctionVal func, int i) { return func->args[i]; }

FunctionPtr get_function(FunctionVal func) { return func->function; }
