# Merjane – Kata de refactoring (entraînement)

Projet d'entraînement inspiré du format du test technique. Le code est volontairement « legacy ».

## Lancer les tests

Prérequis : Java 17+ et Maven 3.9+.

```bash
mvn test
```

## Consignes

Timebox : **2 heures**.

1. Refactorer le code tout en garantissant l'absence de régression.
2. Compléter la couverture de tests sur tous les cas métier (NORMAL, SEASONAL, EXPIRABLE).
3. Respecter les bonnes pratiques (SRP, séparation des couches).
4. Pousser sur un repo public avant la fin de la timebox, même si c'est incomplet.

## Cahier des charges

- Tous les produits ont une valeur `available` (unités en stock) et une valeur `leadTime` (jours de réapprovisionnement).
- À la fin de chaque commande, le système décrémente `available` pour chaque produit commandé.
- **NORMAL** : aucune particularité. En rupture, un délai est annoncé aux clients.
- **SEASONAL** : disponible uniquement à certaines périodes de l'année. En rupture, un délai est annoncé, mais si ce délai dépasse la saison, le produit est considéré comme indisponible et les clients en sont notifiés.
- **EXPIRABLE** : vendu normalement tant qu'il n'a pas expiré, indisponible une fois la date d'expiration passée.

## Point d'entrée

`POST /orders/{orderId}/processOrder`
