var Login = {
    name: "",
    email: "",
    ID: "",
    picture: "",
    joinDate: "",
    handleCredential: function (response) {
        console.log("Login.handleCredential() callback called:" + response.credential)
        // decodeJwtResponse() is a custom function defined by you
        // to decode the credential response.
        const responsePayload = jwt_decode(response.credential);

        console.log("ID: " + responsePayload.sub);
        console.log('Full Name: ' + responsePayload.name);
        console.log('Given Name: ' + responsePayload.given_name);
        console.log('Family Name: ' + responsePayload.family_name);
        console.log("Image URL: " + responsePayload.picture);
        console.log("Email: " + responsePayload.email);

        //DiceGame.name = responsePayload.name
        Login.name = responsePayload.name
        Login.email = responsePayload.email
        Login.ID = response.credential
        Login.picture = responsePayload.picture
        // external event
        m.redraw()
    }
}

var Header = {
    view: function () {
        if (Login.name) {
            return m("header", [
                m(".logo", "PétiQuik"),
                m("nav", [
                    m("ul", [
                        m("li", m("a", { href: "index_petiquik.html#!/home" }, "Accueil")),
                        m("li", m("a", { href: "index_petiquik.html#!/petitions" }, "Pétitions")),
                        m("li", m("a", { href: "#" }, "Actions")),
                        m("li", m("a", { href: "#" }, "Blog")),
                        m("li", m("a", { href: "#" }, "Nous Contacter")),
                    ]),
                ]),
                m(".search-bar", [
                    m("form", { action: "#", method: "get" }, [
                        m("input", {
                            type: "text",
                            name: "search",
                            placeholder: "Recherchez des pétitions...",
                        }),
                        m("button", { type: "submit" }, "Rechercher"),
                    ]),
                ]),
                m("a", { class: "header-profile", href: "index_petiquik.html#!/profile" }, [
                    m("img", { class: "profile-pic", src: Login.picture }),
                    m("p", { style: "margin-left:20px" }, Login.name),
                ]),
            ]);
        } else {
            return m("header", [
                m(".logo", "PétiQuik"),
                m("nav", [
                    m("ul", [
                        m("li", m("a", { href: "index_petiquik.html#!/home" }, "Accueil")),
                        m("li", m("a", { href: "index_petiquik.html#!/petitions" }, "Pétitions")),
                        m("li", m("a", { href: "#" }, "Actions")),
                        m("li", m("a", { href: "#" }, "Blog")),
                        m("li", m("a", { href: "#" }, "Nous Contacter")),
                    ]),
                ]),
                m(".search-bar", [
                    m("form", { action: "#", method: "get" }, [
                        m("input", {
                            type: "text",
                            name: "search",
                            placeholder: "Recherchez des pétitions...",
                        }),
                        m("button", { type: "submit" }, "Rechercher"),
                    ]),
                ]),
                m("div", {
                    "id": "g_id_onload",
                    "data-client_id": "115500541537-rapfqet1t2qmbv79oqc5dcfqdebopj1g.apps.googleusercontent.com",
                    "data-callback": "handleCredentialResponse"
                }),
                m("div", {
                    "class": "g_id_signin",
                    "data-type": "standard"
                }),
            ]);
        }
    },
};

function handleCredentialResponse(response) {
    console.log("handleCredentialResponse() callback called:" + response.credential)
    Login.handleCredential(response)
}

var HeroSection = {
    view: () =>
        m(".hero-section", [
            m(".hero-content", [
                m(
                    "h1",
                    "Rejoignez des millions de personnes pour faire une différence"
                ),
                m(
                    "p",
                    "Nous sommes une communauté mondiale de personnes qui se rassemblent pour changer le monde. Nous avons le pouvoir de faire une différence, un vote à la fois. Rejoignez-nous et devenez le changement que vous voulez voir dans le monde."
                ),
                m("a", { class: "button-big", href: "index_petiquik.html#!/petitions" }, "Voir toutes les pétitions"),
            ]),
            m(".hero-image", [
                m("img", {
                    src: "https://picsum.photos/500/300",
                    alt: "Image de personnes manifestant pacifiquement",
                }),
            ]),
        ]),
};

var allPetitions = {
    list: [],
    loadList: function () {
        return m.request({
            method: "GET",
            url: "_ah/api/petiQuik/v1/top100/"
        }).then(function (result) {
            allPetitions.list = result.items;
            console.log("got:", result.items);
            // m.redraw();
        })
    },
};

var popularPetitions = {
    list: [],
    loadList: function () {
        return m.request({
            method: "GET",
            url: "_ah/api/petiQuik/v1/top4/"
        }).then(function (result) {
            popularPetitions.list = result.items;
            console.log("got:", result.items);
            // m.redraw();
        })
    },
}

var PopularPetitionsView = {
    oninit: popularPetitions.loadList,
    view: function () {
        return m(".petitions", [
            m("h2", "Pétitions populaires"),
            m("ul",
                popularPetitions.list.map(function (petition) {
                    return m(Petition, petition);
                })
            ),
        ]);
    },
};

var AllPetitionsView = {
    oninit: allPetitions.loadList,
    view: function () {
        return m(".petitions", [
            m("h2", "Nos pétitions"),
            m("ul", allPetitions.list.map(function (petition) {
                return m(Petition, petition);
            })),
        ]);
    },
};

var user = {
    firstName: "John",
    lastName: "Doe",
    profilePic: "https://picsum.photos/200",
    joinDate: "2022-01-01",
    petitionsSigned: [
        {
            title: "Stopper la pollution plastique",
            description: "Nous demandons aux gouvernements du monde entier de prendre des mesures pour réduire la pollution plastique qui menace la faune et la flore marine.",
            signatures: 25000,
            image: "https://example.com/images/petition1.jpg"
        },
        {
            title: "Pour une éducation inclusive",
            description: "Nous demandons aux écoles et universités du monde entier de mettre en place des politiques d'inclusion pour les étudiants avec des besoins spécifiques.",
            signatures: 18000,
            image: "https://example.com/images/petition2.jpg"
        },
        {
            title: "Sauvons les abeilles",
            description: "Nous demandons aux gouvernements et aux entreprises de prendre des mesures pour protéger les abeilles, qui sont essentielles pour la pollinisation de nombreux aliments.",
            signatures: 35000,
            image: "https://example.com/images/petition3.jpg"
        }
    ]
};

var petitions = [
    {
        title: "Stopper la pollution plastique",
        description: "Nous demandons aux gouvernements du monde entier de prendre des mesures pour réduire la pollution plastique qui menace la faune et la flore marine.",
        signatures: 25000,
        image: "https://example.com/images/petition1.jpg"
    },
    {
        title: "Pour une éducation inclusive",
        description: "Nous demandons aux écoles et universités du monde entier de mettre en place des politiques d'inclusion pour les étudiants avec des besoins spécifiques.",
        signatures: 18000,
        image: "https://example.com/images/petition2.jpg"
    },
    {
        title: "Sauvons les abeilles",
        description: "Nous demandons aux gouvernements et aux entreprises de prendre des mesures pour protéger les abeilles, qui sont essentielles pour la pollinisation de nombreux aliments.",
        signatures: 35000,
        image: "https://example.com/images/petition3.jpg"
    },
    {
        title: "Pour un internet libre",
        description: "Nous demandons aux gouvernements et aux entreprises de protéger la neutralité du net pour garantir un accès libre et égalitaire à l'information pour tous.",
        signatures: 12000,
        image: "https://example.com/images/petition4.jpg"
    }
];


const ProfileView = {
    view: function (vnode) {
        const user = vnode.attrs.user;
        const petitions = vnode.attrs.petitions;

        return m('.div', [
            m('.profile-card', [
                m('.profile-image', [
                    m('img', { src: user.picture })
                ]),
                m('.profile-info', [
                    m('h2.profile-name', user.name),
                    m('p.profile-date', 'Membre depuis ' + user.joinDate),
                ]),
            ]),
            m('div', [
                 m('h3.profile-petitions-header', 'Pétitions signées'),
                m('ul.profile-petitions-list', petitions.map(function (petition) {
                    return m('li', [
                        m('a', { href: petition.url }, petition.title)
                    ])
                }))
            ])
        ]);

    }
};

const PetitionView = {
    oninit: function (vnode) {
        return m.request({
            method: "GET",
            url: "_ah/api/petiQuik/v1/getPetition/"+vnode.attrs.id,
        }).then(function (result) {
            console.log("got:", pet);
            // m.redraw();
        })
    },
    view: function (vnode) {
        pet = {
            "key": {
              "kind": "Petition",
              "appId": "e~tp-dev-cloud",
              "id": "0",
              "name": vnode.attrs.id,
              "namespace": "",
              "complete": true
            },
            "appId": "e~tp-dev-cloud",
            "namespace": "",
            "kind": "Petition",
            "properties": {
              "owner": "U373",
              "date": "2023-05-29T21:35:31.953Z",
              "image": "https://picsum.photos/200/150",
              "nbvotants": "190",
              "name": "Pour le retour de Groquik",
              "description": "skjdhfkqjhglkjsqdfjldg lqksj",
              "votants": [
                "U958",
                "U319"
              ],
              "tags": [
                "T85",
                "T40"
              ]
            }
          };
        return m("div", [
            m(Petition, pet),
        ]);
    }
};


var Petition = {
    view: function (vnode) {
        return m("li", [
            m(".petition-image", [
                m("img", { src: vnode.attrs.properties.image, alt: vnode.attrs.properties.name }),
            ]),
            m(".petition-content", [
                m("h3", vnode.attrs.properties.name),
                m("p", vnode.attrs.properties.description),
                m("a", {href: "index_petiquik.html#!/petition/"+vnode.attrs.key.name }, "LIEN"+vnode.attrs.key.name),
                m("div.signature-count", vnode.attrs.properties.nbvotants + " signataires"),
            ]),
            m(".sign", [
                m("a", { class: "button-small", href: "index_petiquik.html#!/petitions" }, "Signer"),
            ])
        ]);
    },
};

var Footer = {
    view: function () {
        return m("footer", [
            m("p", "PetiQuik © 2023 Tous droits réservés."),
            m("a", { href: "https://facebook.com", target: "_blank" }, "Politique de confidentialité"),
            m("a", { href: "#", target: "_blank" }, "Conditions d'utilisation")
        ]);
    }
}

var HomePage = {
    view: function () {
        return m("body", [
            m(Header),
            m(HeroSection),
            m(PopularPetitionsView),
            m(Footer)
        ]);
    },
};

var AllPetitionsPage = {
    view: function () {
        return m("body", [
            m(Header),
            m(AllPetitionsView),
            m(Footer),
        ]);
    },
};

var PetitionPage = {
    view: function (vnode) {
        var id = vnode.attrs.id; // Récupère l'ID de la pétition depuis les attributs du vnode
    
        return m("body", [
          m(Header),
          m(PetitionView, { id: id }), // Passe l'ID de la pétition à la vue de la pétition
          m(Footer)
        ]);
    }
};

var ProfilePage = {
    view: function () {
        return m("body", [
            m(Header),
            m(ProfileView, { user: Login, petitions: popularPetitions.list }),
            m(Footer)
        ]);
    },
};

m.route(document.body, "/home", {
    "/home": HomePage,
    "/petitions": AllPetitionsPage,
    "/profile": ProfilePage,
    "/petition/:id": PetitionPage
})