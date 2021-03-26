#include <sgr.h>
#include <stdio.h>
#include <stdlib.h>

#include "model/businesses.h"
#include "model/reviews.h"
#include "model/users.h"
#include "stats.h"

struct sgr {
    UserCollection catalogo_users;
    BusinessCollection catalogo_businesses;
    ReviewCollection catalogo_reviews;
    Stats estatisticas;
};
struct TABLE {};

SGR load_sgr(char* users, char* businesses, char* reviews) {
    FILE* fp_users = fopen(users, "r");
    FILE* fp_businesses = fopen(businesses, "r");
    FILE* fp_reviews = fopen(reviews, "r");

    SGR sgr = malloc(sizeof(struct sgr));
    //*sgr = (struct sgr){
    //    .catalogo_users = ler_users(fp_users),
    //    .catalogo_businesses = ler_businesses(fp_businesses),
    //    .catalogo_reviews = ler_reviews(catalogo_reviews)};
    // sgr->estatisticas = calcula_estatisticas(sgr);

    fclose(fp_users);
    fclose(fp_businesses);
    fclose(fp_reviews);
    return sgr;
}

int main(int argc, char* argv[]) {}
