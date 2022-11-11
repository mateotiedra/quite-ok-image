- La fonction testEncodeDataWithImages a été crée afin de tester que le CONTENU des images encodées sont bien similaires à celles des références (sans prendre en compte les headers, channels, etc). Si cela n'est pas le cas la fonction affiche où apparaît la première différence et qu'est ce qui est expected à cet endroit.

- Les fonctions testQoiFile et testDecodeQoiFile vérifient simplement que le fichier crée soient égal au fichier attendu.

- Afin d'ajouter des images à tester il suffit d'ajouter d'en ajouter dans le dossier reference, une fois au format png et une fois au format qoi (pour corriger)
