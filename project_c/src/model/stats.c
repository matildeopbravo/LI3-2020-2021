#include "stats.h"

#include <stdbool.h>
#include <string.h>

#include "auxiliary.h"
#include "glib.h"
#include "table.h"

typedef struct stars_tuple {
  float current_average; // current start average
  size_t number_reviews; // number of reviews read up to this point
} * StarsTuple;

// Mudar nome
typedef struct city_tuple {
  float stars; // current start average
  char *business_id;
  char *name;
} * CityTuple;

struct stats {
  GHashTable *business_id_to_stars; // updated as a new review is read,
                                    // business_id to a StarsTuple
  GHashTable
      *city_to_business_by_star; // linked list of businesse
                                 // ssorted decrescently by stars (city_tuple)
  GHashTable *category_to_business_by_star; // linked list of businesse
                                            // ssorted decrescently by
                                            // category (city_tuple)
};

Stats start_statistics() {
  Stats stats = malloc(sizeof(struct stats));
  stats->business_id_to_stars = g_hash_table_new(g_str_hash, g_str_equal);
  stats->city_to_business_by_star = g_hash_table_new(g_str_hash, g_str_equal);
  stats->category_to_business_by_star =
      g_hash_table_new(g_str_hash, g_str_equal);
  return stats;
}

// when a new review is read this function is called
void update_average_stars(Stats stats, char *business_id, float new_star) {
  GHashTable *business_hash = stats->business_id_to_stars;
  StarsTuple tuplo = g_hash_table_lookup(business_hash, business_id);
  if (!tuplo) {
    tuplo = calloc(1, sizeof(struct stars_tuple));
    g_hash_table_insert(business_hash, g_strdup(business_id), tuplo);
  }
  if (tuplo->number_reviews == 0) {
    tuplo->number_reviews++;
    tuplo->current_average = new_star;
  } else {
    tuplo->current_average =
        ((float)((tuplo->current_average * tuplo->number_reviews) + new_star)) /
        (tuplo->number_reviews + 1);
    tuplo->number_reviews++;
  }
}

float get_average_number_stars(Stats stats, char *business_id) {
  if (!stats || !business_id)
    return (-1);
  StarsTuple tuple = ((StarsTuple)g_hash_table_lookup(
      stats->business_id_to_stars, business_id));
  if (!tuple)
    return (-1);
  return tuple->current_average;
}

bool is_empty_stats(Stats stats) {
  int empty = true;
  if (stats)
    empty = false;
  return empty;
}

bool is_empty_business_id_to_stars(Stats stats) {
  int empty = true;
  if (stats && stats->business_id_to_stars)
    empty = false;
  return empty;
}

void start_table_iter_init_business_id_hash_table(GHashTableIter *iter,
                                                  Stats stats) {
  if (!stats || !iter)
    return;
  g_hash_table_iter_init(iter, stats->business_id_to_stars);
}

CityTuple init_city_tuple(float stars, char *business_id, char *name) {
  CityTuple tuplo = calloc(1, sizeof(struct city_tuple));
  tuplo->business_id = g_strdup(business_id);
  tuplo->stars = stars;
  tuplo->name = g_strdup(name);
  return tuplo;
}

CityTuple copy_city_tuple(CityTuple self) {
  CityTuple tuplo = calloc(1, sizeof(struct city_tuple));
  tuplo->business_id = g_strdup(self->business_id);
  tuplo->stars = self->stars;
  tuplo->name = g_strdup(self->name);
  return tuplo;
}

void free_city_tuple(CityTuple self) {
  if (!self)
    return;
  free(self->business_id);
  free(self->name);
  free(self);
}

void free_g_city_tuple(gpointer data) { free_city_tuple((CityTuple)data); }

void free_stats(Stats stats) {
  if (!stats)
    return;

  g_hash_table_foreach(stats->business_id_to_stars, (void *)free, NULL);
  g_hash_table_destroy(stats->business_id_to_stars);
  g_hash_table_foreach(stats->city_to_business_by_star,
                       (void *)free_g_city_tuple, NULL);
  g_hash_table_destroy(stats->city_to_business_by_star);
  g_hash_table_foreach(stats->category_to_business_by_star,
                       (void *)free_g_city_tuple, NULL);
  g_hash_table_destroy(stats->category_to_business_by_star);
}

// ver melhor
gint compare_stars(gconstpointer key1, gconstpointer key2, gpointer user_data) {
  return (((CityTuple)key2)->stars - ((CityTuple)key1)->stars) * 100;
}

void init_city_to_business_by_star(Stats stats) {
  if (stats && !stats->business_id_to_stars)
    stats->business_id_to_stars = g_hash_table_new(g_str_hash, g_str_equal);
}

void init_category_to_business_by_star(Stats stats) {
  if (stats && !stats->category_to_business_by_star)
    stats->category_to_business_by_star =
        g_hash_table_new(g_str_hash, g_str_equal);
}

// debugging prints
void print_node(gpointer data, gpointer user_data) {
  CityTuple x = (CityTuple)data;
  printf("City %s, business : %f\n", x->name, x->stars);
}

void print_list(GSList *list) { g_slist_foreach(list, print_node, NULL); }

void add_city_to_business_by_star(Stats stats, char *city, char *business_id,
                                  float stars, char *name) {
  if (!stats || !stats->business_id_to_stars)
    return;

  if (!stats->city_to_business_by_star)
    stats->city_to_business_by_star = g_hash_table_new(g_str_hash, g_str_equal);

  CityTuple value = init_city_tuple(stars, business_id, name);

  GSList *aux = g_hash_table_lookup(stats->city_to_business_by_star, city);

  // if aux is null,  , append will create
  aux = g_slist_insert_sorted_with_data(aux, value, compare_stars, NULL);
  g_hash_table_insert(stats->city_to_business_by_star, g_strdup(city), aux);
}

void add_category_to_business_by_star(Stats stats, char *category,
                                      char *business_id, float stars,
                                      char *name) {
  if (!stats || !stats->business_id_to_stars) {
    return;
  }

  if (!stats->category_to_business_by_star)
    stats->category_to_business_by_star =
        g_hash_table_new(g_str_hash, g_str_equal);

  CityTuple value = init_city_tuple(stars, business_id, name);

  GSList *aux =
      g_hash_table_lookup(stats->category_to_business_by_star, category);

  aux = g_slist_insert_sorted_with_data(aux, value, compare_stars, NULL);

  g_hash_table_insert(stats->category_to_business_by_star, g_strdup(category),
                      aux);
}

void build_city_and_category_hash_table(BusinessCollection const businesses,
                                        Stats stats) {

  /* printf("%d\n", is_empty_business_id_to_stars(sgr->estatisticas)); */

  if (!businesses || !stats || !stats->business_id_to_stars)
    return;

  GHashTableIter iter;

  char *key = NULL;
  StarsTuple value = NULL;

  g_hash_table_iter_init(&iter, stats->business_id_to_stars);

  while (g_hash_table_iter_next(&iter, (gpointer *)&key, (gpointer *)&value)) {

    if (!key)
      continue;

    char *business_id = g_strdup(key);
    float current_average = value->current_average;

    Business business =
        get_businessCollection_business_by_id(businesses, business_id);

    if (!business)
      continue;

    char *city = get_business_city(business);

    char *name = get_business_name(business);

    if (!city || !name)
      continue;

    GPtrArray *categories = get_business_categories(business);

    add_city_to_business_by_star(stats, city, business_id, current_average,
                                 name);

    int size = categories ? categories->len : 0;
    for (int i = 0; i < size; i++)
      add_category_to_business_by_star(stats, g_ptr_array_index(categories, i),
                                       business_id, current_average, name);
    free(city);
    free(name);
    free(business_id);
    free_business(business);
  }
}

static GSList *n_larger_gs_list(int N, GSList *gs_list) {
  GSList *new = NULL;
  int size = g_slist_length(gs_list);

  for (int i = 0; i < size && i < N; i++)
    new = g_slist_append(new, copy_city_tuple(g_slist_nth_data(gs_list, i)));

  return new;
}

static GSList *n_larger_than_gs_list(int N, GSList *gs_list) {
  GSList *new = NULL;
  int stop = 0;
  int size = g_slist_length(gs_list);

  for (int i = 0; !stop && i < size; i++) {
    CityTuple cont = g_slist_nth_data(gs_list, i);

    if (cont->stars < N) {
      stop = 1;
      continue;
    }
    new = g_slist_append(new, copy_city_tuple(cont));
  }
  return new;
}

void n_larger_city_star(Stats stats, char *city, int N, TABLE table,
                        int larger_than) {
  if (!stats || !stats->city_to_business_by_star) {
    return;
  }

  GSList *list =
      larger_than
          ? n_larger_than_gs_list(
                N, g_hash_table_lookup(stats->city_to_business_by_star, city))
          : n_larger_gs_list(
                N, g_hash_table_lookup(stats->city_to_business_by_star, city));
  if (!list)
    return;

  CityTuple value;
  int size = g_slist_length(list);

  for (int i = 0; i < size; i++) {
    value = (CityTuple)g_slist_nth_data(list, i);
    char *id = value->business_id;
    char *name = value->name;
    char *stars = g_strdup_printf("%f", value->stars);

    add_field(table, id);
    add_field(table, name);
    if (larger_than == 0)
      add_field(table, stars);

    free(stars);
    i++;
  }

  if (larger_than) {
    char *size_str = g_strdup_printf("%d", size);
    add_footer(table, "Número total: ", size_str);
    // apagar
    add_field(table, size_str);
    free(size_str);
  }

  /* if (list) */
  /*   g_slist_free(list); */
}

void all_n_larger_than_city_star(Stats stats, int N, TABLE table) {
  if (!stats || !stats->city_to_business_by_star) {
    return;
  }

  GList *all_keys = g_hash_table_get_keys(stats->city_to_business_by_star);

  if (!all_keys)
    return;

  int size = g_list_length(all_keys);

  for (int i = 0; i < size; i++) {
    char *city = g_strdup(g_list_nth_data(all_keys, i));
    n_larger_city_star(stats, city, N, table, 0);
    free(city);
  }
  // free all_key??
  /* if (all_keys) */
  /*   g_list_free(all_keys); */
}

void n_larger_category_star(Stats stats, char *category, int N, TABLE table) {
  if (!stats || !stats->city_to_business_by_star) {
    return;
  }

  GSList *list =
      g_hash_table_lookup(stats->category_to_business_by_star, category);

  if (!list || g_slist_length(list) < 1) {
    return;
  }

  int size = g_slist_length(list);

  CityTuple value;
  int i;
  for (i = 0; i < size && i < N; i++) {

    value = (CityTuple)g_slist_nth_data(list, i);

    char *id = value->business_id;
    char *name = value->name;
    char *stars = g_strdup_printf("%f", value->stars);

    add_field(table, id);
    add_field(table, name);
    add_field(table, stars);
    free(stars);
  }

  char *size_str = g_strdup_printf("%d", i);
  add_footer(table, "Número total: ", size_str);
  // apagar
  add_field(table, size_str);
  free(size_str);

  // free do list
  /* if (list) */
  /*   g_slist_free(list); */
}
