# Implémentation du moteur de lignes de produits

Le moteur de ce projet est responsable de la gestion automatisée de la
ligne de produits. Basé sur le langage Java, ce programme peut à l'aide
d'un fichier de configuration, détaillé dans une des sections suivantes,
compiler une configuration donnée d'une ligne de produits.

## Use cases

### Conception d'une ligne de produits

Concevoir une ligne de produits pour qu'elle puisse être utilisée par ce
moteur se fait en plusieurs étapes.

1.  Conception de la ligne de produits: la ligne de produits doit
    s'articuler autour d'une partie commune sur laquelle les différents
    variants viennent s'ajouter ou s'enlever.

2.  Définition du FeatureModel: un fichier \"*FeatureModel.xml*\" doit
    être défini à la racine de la partie principale de la ligne de
    produits. Comme expliqué dans le chapitre \"XML.FeatureModel\", ce
    fichier définit les différents fichiers à ajouter ou actions à
    effectuer pour lier un variant.

3.  Publication: la ligne de produits doit être publiée soit sur un
    repository Github soit dans un dossier Windows en respectant les
    règles expliquées dans le chapitre \"Import\".

### Sélectionner une configuration d'une ligne de produits

Toute personne voulant utiliser ce moteur de ligne de produits devrait
suivre les étapes suivantes:

1.  Prendre connaissance des variants disponibles pour la ligne de
    produits donnée.

2.  Définir la configuration voulue dans un fichier xml en respectant
    les règles détaillées dans le chapitre \"XML.Configuration\".

3.  Executer le moteur en fournissant le fichier de configuration.

Une personne qui voudrait obtenir sa configuration devrait exécuter la
commande suivante:


java -jar engine.jar \<chemin du fichier de configuration\>\<exécution des tests\>

Le premier paramètre est constitué simplement du chemin vers le fichier
xml contenant le configuration voulue. Le paramètre suivant d'exécution
des tests à été ajouté afin de permettre de ne pas exécuter de test lors
de la création du package jar. Lors du packaging de l'implémentation de
test décrite dans le chapitre suivant, une erreur est survenue. Durant
la génération d'une application utilisant le framework Spring, ce
dernier possède des tests qui seront exécutés lorsque Maven génère le
fichier jar. Un des tests de Spring consiste en la vérification de la
connexion à la base de données utilisée par l'application. Le moteur de
ligne de produits ne pouvant pas connaitre tous les frameworks qui
effecturaient ce genre de tests, il s'est montré plus pertinent
d'ajouter un paramètre pour faire abstraction des tests lors de la
génération du fichier jar par Maven. Ce paramètre est un boolean dont la
valeur true fait abstraction des tests.

### Vérification d'un featureModel

Afin d'aider à la bonne écriture du featureModel, sur lequel dépend la
capacité du moteur à créer une configuration, celui-ci peut effectuer
une vérification de la structure. Tout plugin doit implémenter une
méthode nommée \"*getxsdDeclaration*\" qui a responsabilité de fournir
du code XSD permettant de valider la structure du featureModel pour les
parties concernant ce plugin. Pour ce faire, il suffit de remplacer le
paramètre fournissant le chemin vers un fichier de configuration par un
chemin vers le featureModel à vérifier.

java -jar engine.jar \<chemin du fichier featureModel\>


## Architecture

L'utilisation de plugin a semblé d'une grande importance dans ce projet
étant donné le grand nombre de techniques de variation déjà existantes
et les nombreuses futures techniques ayant des fins de variabilité.
C'est pourquoi ce moteur de ligne de produits tourne autour de
l'interface \"*Interpreter*\" qui définit le minimum de méthodes
nécesaires à tout plugin voulant fonctionner avec ce projet. Le
diagramme de classes de ce projet se présente comme ci dessous:
[image]{.image}

## XML

Après de longues réflexions, il a été décidé qu'écrire un langage avec
un outil tel que Antlr s'avèrerait relativement complexe et couteux en
temps. L'approche XVCL a paru une solution plus pertinente avec un
meilleur potentiel d'évolution ainsi qu'une meilleure ouverture pour des
techniques de variabilité pas encore implémentées. XML est donc utilisé
comme base afin de définir les différentes caractéristiques d'une ligne
de produits et pour sélectionner les différents variants souhaités pour
une configuration.\

La définition de la ligne de produits et le choix de la configuration
choisie se fait dans deux fichiers distincts.

### FeatureModel

Le featureModel est écrit dans un fichier *'FeatureModel.xml'* et
contient la définition des différents variants qui existent pour la
ligne de produits. Ce fichier doit obligatoirement se trouver à la
racine pour des soucis de gestion de fichier et est présenté comme ceci:

La structure du fichier XML, comprend l'élément FeatureModel comme
racine ainsi que les différent variant présent à l'intérieur. Tout
variant doit obligatoirement posséder l'attribut \"*name*\" afin de
pouvoir être identifier dans le fichier de configuration. Il est
également possible de définir avec l'attribut \"*require*\" une
dépendance avec un autre variant. L'attribut \"*denied*\" permet enfin
de définir deux variants ne pouvant pas cohabiter dans une même
configuration.

### Configuration

Le moteur de ligne de produits accepte une configuration de ligne de
produits sous forme d'un fichier xml dont le choix du nom est laissé
libre. Ce fichier contient la liste des différents variants souhaités.
Certaines techniques de variation, pourraient avoir besoin de
paramètres. C'est le cas du plugin *SpringPreprocessor* qui nécessite la
transmission d'une valeur par le fichier de configuration. Ce fichier
est présenté comme ceci:

::: {.center}
[image]{.image}
:::

## Validation du XML

Le format de fichier XML est utilisé de nos jours comme un moyen
d'organiser de l'information à transmettre par exemple par internet.
Dans un premier temps, il est important de vérifier qu'un document donné
respecte bien la syntaxe du XML pour être qualifié de bien formé. Il
faut également vérifier que celui-ci respecte la structure désirée afin
d'être valide[@DTD]. Pour ce faire, a été inventé en 1998 le langage
DTD(Document Type Definition), cependant, ce langage s'est avéré limité
en vérification pour de multiple raisons[@XSDBook]. C'est pourquoi
d'autres langages et outils ont été proposés dont XSD, SOX, XSV,
\...[@TestXml].\

Etant donné que ce moteur de ligne de produit utilise le format XML pour
définir un featureModel et également pour sélectionner une configuration
voulue, il a été nécessaire de pouvoir efficacement valider ces
derniers. Pour ce faire, XSD a semblé le meilleur choix étant donné sa
popularité et ses fonctionnalités qui correspondent tout à fait aux
besoins. Le moteur de ligne de produits ne pouvant pas connaître la
structure XML définie par les plugins, ceux-ci doivent donc communiquer
la structure XML qu'ils attendent respectivement. Ainsi, le moteur qui
possède la structure de base pour tout featureModel peut assembler les
morceaux des différents plugins et valider ou non tout featureModel
qu'il rencontre.

### DTD

DTD fut le premier langage dévoilé en 1998 ayant pour but de définir la
structure d'un document XML. Un fichier DTD n'est pas obligatoire à
l'existance d'un fichier XML. Depuis, d'autres langages ont été proposés
en remplacement à cause des différentes limitations de DTD et à
l'évolution de l'utilisation du XML [@TestXml]. La grande difficulté de
ce langage réside dans le fait que celui-ci possède une syntaxe
différente du XML ce qui rend plus complexe son utilisation comparé par
exemple à XDS qui utilise la même syntaxe que le XML. DTD ne fut pas
qualifié de facile à utiliser[@DTD].

### XSD

L'organisme de standardisation W3C en 2001 a introduit le langage XSD
(XML Schéma Définition). Basé sur le langage XML, XSD a pour but de
décrire formellement la structure d'un fichier XML. Ceci devant rendre
utilisable le format XML de façon fiable pour échanger de l'information
et éviter autrement de nombreux tests pour s'assurer de la conformiter
du document[@XSDBook]. Il est possible de définir exactement le type de
données qui doit s'y trouver qu'il soit simple, complexe ou
compositionnel de type séquence, choix ou tous les enfants. Les
attributs peuvent également être définis et il est possible de les
rendre obligatoires, optionnels ou prohibés[@TestXml; @XSDBook]. Les
deux grands avantages de XDS par rapport à DTD sont[@comparXMLS]:

-   Les capacités et fonctionnalités plus avancées pour définir le type
    de données attendu dans le document XML

-   un document XDS s'écrit en utilisant la syntaxe XML ce qui facilite
    grandement son apprentisage et utilisation car éditable avec un
    simple éditeur xml.

Ci-dessous une partie du fichier XDS permettant au moteur de ligne de
produits de valider le fichier XML qui contient un featureModel.
Celui-ci est composé par les différents plugins installés qui
fournissent le XDS nécessaire à valider les parties de ce featureModel
les concernant. C'est la méthode \"*getxsdDeclaration*\" présent dans
l'interface \"*Interpreter*\" qui permet de récupérer auprès de chaque
plugins le XDS les concernant.

### SOX

Développé par Commerce One, SOX est un autre langage permettant de
définir la structure et sémantique d'un fichier XML. La particularité de
ce langage par rapport à son ancêtre DTD est l'apport de la notion
d'objet. Cette notion apporte comme dans la programmation des
possibilités d'héritage ainsi que de types de données ayant une certaine
hiérarchie. Tel que XSD, SOX offre l'avantage d'utiliser la syntaxe
XML[@comparXMLS].

## Plugin

Il a été choisi de laissé ouverts les types de variants. C'est pourquoi
la méthode des plugins a été utilisée afin que de nouveaux types de
connexions de variants puissent être utilisés. Ceux ci devront
implémenter une interface comprenant ces méthodes:

-   *getName*: permet d'identifier le plugin à appliquer sur un variant
    défini dans un deux fichiers xml.

-   *getxsdDeclaration*: afin de vérifier la bonne struture d'un variant
    dans le featureModel, cette méthode fournit le XSD permettant de
    valider chaque appel dans le featureModel à ce plugin.

-   *checImport*: vérifie que les différents fichiers à importer dans la
    configuration sont bien importés.

-   *construct*: méthode principale de chaque plugin, c'est cette
    méthode qui lance la liaison du variant.

-   *setConfigFile*: permet au variant d'accéder au fichier de
    configuration pour, par exemple, fournir une variable.

Les plugins implémentés au moment de la remise de ce mémoire sont au
nombre de quatre. Ceci pour valider le moteur sur plusieurs techniques
de variabilités connues des développeurs. La classe \"*PluginLoader*\"
est responsable de charger le dossiers \"*plugins*\" où devront être
déposés les plugins à utiliser. Ceux-ci devront se présenter sous forme
de fichiers *.jar* contenant le nécessaire du plugin. Chargés en
mémoire, ils sont ensuite instanciés s'ils implémentent correctement
l'interface \"*interpreter*\".

### SpringAspect

Ce plugin a comme devoir de gérer la programmation orientée aspect en
utilisant le framework Spring qui offre un module qui prend en charge ce
paradigme. La programmation orientée aspect étant très accessible avec
Spring, le plugin de ce moteur pour la programmation orientée aspect ne
doit qu'importer les fichiers Java contenant les aspects désirés.\

La structure de ce plugin dans le fichier *FeatureModel* doit traduire
l'ajout du fichier contenant le code de l'aspect à ajouter. C'est
pourquoi l'élement *SpringAspect* avec le paramètre *name* définissant
le nom du variant en question, contient un élément *file* avec le
paramètre *path* contenant le chemin depuis la racine du dossier de ce
variant vers le fichier contenant l'aspect. Dans le fichier de
configuration, il suffit de définir un élément xml nommé *SpringAspect*
avec le paramètre *name* où le nom du variant doit être spécifié.

### Delta

L'implémentation du plugin de programmation orientée delta fut le plugin
le plus long à implémenter, compte tenu du fait des capacités
nécessaires afin de prétendre pouvoir faire de la programmation delta.\

La structure du fichier *FeatureModel* pour la définition d'un variant
utilisant la programmation delta se divise en trois parties pour les
trois opérations autorisées lors de la liaison du variant.

1.  *addFile* permet d'ajouter une liste de fichiers définis par des
    éléments xml *file* avec un paramètre *path* contenant le chemin
    vers le fichier depuis la racine du dossier de ce variant.

2.  *deleteFile* permet de supprimer une liste de fichiers définis par
    des éléments xml *file* avec un paramètre *path* contenant le chemin
    vers le fichier depuis la racine du dossier de ce variant.

3.  *modif* permet de modifier une liste de fichiers définis par des
    éléments xml *file* avec un paramètre *path* contenant le chemin
    vers le fichier depuis la racine du dossier de ce variant ainsi
    qu'un paramètre *type* permettant de fournir au plugin le nom du
    langage à des fins de traitement avec la grammaire du langage
    concerné. Un sous-élément *add* contient enfin le contenant à
    ajouter au fichier.

Le fichier de configuration doit simplement contenir un élément nomé
*Delta* avec un paramètre *name* contenant le nom du variant.

### Plugin

Comme le plugin pour la programmation orientée aspect, le plugin
permettant la gestion des plugins dans la ligne de produits doit
uniquement pouvoir importer le fichier JAR qui constitue le plugin à
importer. Pour le projet servant à valider ce moteur de ligne de
produits, les plugins doivent se trouver dans un dossier \"plugins\"
cependant pour d'autres lignes de produits, ces plugins peuvent devoir
se trouver dans un autre dossier.\

De même que le plugin *SpringAspect*, la structure de ce plugin dans le
fichier *FeatureModel* traduit l'ajout du fichier *.jar* contenant le
plugin à ajouter. C'est pourquoi l'élement *Plugin* avec le paramètre
*name* qui définit le nom du variant en question, contient au minimum un
élément *file* avec le paramètre *path* contenant le chemin depuis la
racine du dossier de ce variant vers le fichier contenant le plugin.
Dans le fichier de configuration, il suffit de définir un élément xml
nommé *Plugin* avec le paramètre *name* où le nom du variant doit être
spécifié.

### SpringPreprocessor

Le langage Java n'accepte pas nativement de directives préprocesseur.
Cependant, l'ouverture qu'offre l'implémentation permet de créer une
syntaxe de préprocesseur et de faire fonctionner la liaison d'un variant
donné avec des directives préprocesseur comme dans les langage tels que
C. La syntaxe pensée pour ce plugin SpringPréprocessor accepte deux
types de directives préprocesseur. Chacune de ces directives peut être
influencée par une variable transmise depuis le fichier de
configuration.

-   Condition: la syntaxe *\#\#ifdef* permet de définir une condition
    d'inclusion. La syntaxe *\#\#else* permet de définir un block de
    code alternatif tandis que la syntaxe *\#\#endif* permet d'indiquer
    la fin de la condition.

-   Définition: Il est possible de définir une constante avec le préfixe
    *\#\#define* et chaque itération de cette variable dans le fichier
    sera remplacée par sa valeur.

Le *FeatureModel* qui définit un variant utilisant *SpringPreprocessor*
doit posséder un paramètre *name* spécifiant le nom du variant, dans
l'élément principal. Chaque fichier possèdant une directive
préprocesseur à manipuler, doit être listé par un élément *file* avec le
paramètres *path* contenant le chemin depuis la racine du dossier de ce
variant. A l'intérieur de cet élément, se trouve un ou plusieurs
éléments *var* avec comme nom de paramètre, le nom de la variable et
comme valeur de ce paramètre, la valeur choisie par défaut. Cette
variable peut être modifiée dans le fichier de configuration afin de lui
donner une valeur précise.

## Import

Il a été décidé de permettre deux types d'imports où la structure et les
fichiers de la ligne de produits sont stockés. Le premier système de
fichiers autorisé est un dossier Windows défini par un path avec un
dossier spécifique pour chaque variant. Le deuxième système de fichiers
accepté est Github utilisant le système de branche afin de séparer les
fichiers des différents variants.

### Github

Depuis le fichier de configuration, il est possible d'importer une ligne
de produits qui est sauvegardée sur un repository Github. Afin que la
configuration puisse fonctionner, chaque variant devra néanmoins être
sauvegardé sur une branche nommée avec le même nom que celui spécifié
dans le FeatureModel. Le fichier FeatureModel doit être à la racine de
la branche *main*.

### Directory

Afin de permettre d'utiliser le système de fichiers Windows, il a été
nécessaire de définir une certaine structure à respecter.

Comme dans le graphe ci-dessus, la structure des dossiers doit être
comprise dans un dossier principal. Dans ce dernier, un premier dossier
au nom libre, comprend la base choisie pour la ligne de produits ainsi
que le fichier \"*FeatureModel.xml*\". Les autres dossiers comprenant
chacun les fichiers nécessaires aux différents variants, doivent
obligatoirement être nommés avec le même nom qui est spécifié dans le
FeatureModel.

## Maven

Maven est un outil populaire open source de la fondation Apache, qui
permet d'automatiser des opérations de développement du code Java telles
que la gestion des dépendances ou la construction du package
jar[@SpringBook; @Spring]. C'est cet outil qui est utilisé pour générer
le fichier jar contenant le nécessaire pour faire fonctionner
l'application qui correspond à la configuration de la ligne de produits
choisie. Les commandes à exécuter par maven sont:*mvn build* et *mvn
package*\

La gestion des dépendances dans un projet utilisant Maven se fait dans
un fichier appelé \"*pom.xml*\". Il existe un repository sur internet
permettant de downloader certaines dépendances. Une dépendance Maven
peut être identifiée sans erreur à l'aide d'un *groupId*et un
*artifactId* ainsi qu'une version et éventuellement un type(jar, war,
EAR)[@MVNBook].

## Dépendances

Ce projet utilisant Maven comme gestionnaire de dépendances, celles-ci
sont définies dans un fichier \"*pom.xml*\". Plusieurs dépendances ont
dû être appelées, voici les principales:

-   **jdom2**: Librairie permettant d'accéder et manipuler un fichier
    XML.

-   **jPowerShell**: Librairie pour exécuter des commandes PowerShell.

-   **org.eclipse.jgit**: Librairie offrant certaines manipulations d'un
    repository Github.

-   **jsoup**: Librairie servant de navigateur web dans une console.

-   **junit-jupiter**: Les tests unitaires nécessitent cette librairie.

## Travail futur

Le programme développé et présenté dans ce mémoire permet ainsi de
pouvoir concevoir une ligne de produits avec des variants utilisant la
méthode de variation souhaitée, mais également de pouvoir construire une
configuration et fournir le programme associé. Cela répond aux besoins
énoncés pour ce travail dont la création d'une plateforme ayant les
capacités de générer une configuration pour une ligne de produits
donnée. Cependant, certaines améliorations restent sans conteste
possibles.

### GUI

Le moteur de ligne de produits fonctionne pour l'instant dans une
fenêtre console. Il est tout à fait possible de l'utiliser en l'état.
Pourtant, une utilisation par une personne lambda sans connaissance
informatique nécessite une interface graphique. De plus, certaines
fonctionnalités peuvent alors être imaginées afin de rendre la sélection
d'une configuration plus commode. Lister visuellement les variants
disponibles d'après un featureModel et pouvoir griser dans cette liste
les variants interdits suite à une règle de dépendance peut, par
exemple, grandement faciliter la configuration.

### Plugins

Actuellement, le nombre de techniques de variabilité accepté par le
moteur de ligne de produits est de quatre. Il est évident qu'il existe
davantage de techniques de variabilité, cependant celles qui sont en
place permettent de valider l'utilisation du moteur et grace à
l'utilisation de plugin. Il est tout à fait possible d'ajouter d'autres
méthodes de variabilité.
