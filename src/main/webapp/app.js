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
        if (Login.ID) {
            return m("header", [
                m(".logo", "PétiQuik"),
                m("nav", [
                    m("ul", [
                        m("li", m("a", { href: "index_petiquik.html#!/home" }, "Accueil")),
                        m("li", m("a", { href: "index_petiquik.html#!/petitions" }, "Pétitions")),
                        m("li", m("a", { href: "index_petiquik.html#!/create" }, "Nouvelle pétition")),
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

var myPetitions = {
    list: [],
    loadList: function () {
        return m.request({
            method: "GET",
            url: "_ah/api/petiQuik/v1/mesPetitions"+'?access_token='+Login.ID
        }).then(function (result) {
            myPetitions.list = result.items;
            console.log("myPetitions.list:", result.items);
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

const ProfileView = {
    oninit: myPetitions.loadList,
    view: function (vnode) {
        const user = vnode.attrs.user;
        const petitions = myPetitions.list;

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

var CreateView = {
    name: "",
    description: "",
    objective: 1,
    picture: "",
    tags: [],
  
    submitForm: function () {
        var pet = {
            nom: CreateView.name,
            description: CreateView.description,
            image: CreateView.picture,
            tags: CreateView.tags,
            objectif: CreateView.objective,
        };
        console.log("Données du formulaire :", pet);
        return m.request({
            method: "POST",
            url: "_ah/api/petiQuik/v1/addPetition"+'?access_token='+Login.ID,
            params: pet,
        })
        .then(function(result) {
            console.log("created:",result)
            myPetitions.loadList()
        })
    },
  
    view: function () {
      return m("form", {
        onsubmit: function (e) {
          e.preventDefault();
          CreateView.submitForm();
        },
        class:"create-form"}, [

            m("h1", "Créer une pétition"),
            m("label", "Nom de la pétition"),
            m("input[type=text]", {
            value: CreateView.name,
            oninput: function (e) { CreateView.name = e.target.value; },
            required: true
            }),
    
            m("label", "Description"),
            m("textarea", {
            value: CreateView.description,
            oninput: function (e) { CreateView.description = e.target.value; },
            required: true,
            }),

            m("label", "Photo (400x300)"),
            m("input[type=url]", {
            value: CreateView.picture,
            oninput: function (e) { CreateView.picture = e.target.value; },
            placeholder:"https://example.com",
            pattern:"https://.*",
            required: true,
            }),
    
            m("label", "Objectif de votants"),
            m("input[type=number]", {
            value: CreateView.objective,
            oninput: function (e) { CreateView.objective = e.target.value; },
            required: true,
            min: "1"
            }),
    
            m("label", "Tags (séparés par des virgules)"),
            m("input[type=text]", {
            value: CreateView.tags.join(","),
            oninput: function (e) { CreateView.tags = e.target.value.split(","); },
            required: true,
            }),
            
            m("button[type=submit]",{class: "create-button", style: "margin:20px;"} ,"Créer la pétition")
      ]);
    }
  };

const PetitionView = {
    pet: {
        "key": {
          "id": "5632499082330112",
        },
        "properties": {
          "image": "",
          "dateCreation": "",
          "objectif": "",
          "description": "",
          "datetri": "",
          "nom": "",
          "nbVotants": "",
          "createurId": "",
          "tags": [],
        }
    },

    oninit: function (vnode) {
        return m.request({
            method: "GET",
            url: "_ah/api/petiQuik/v1/getPetition/"+vnode.attrs.id,
        }).then(function (result) {
            PetitionView.pet = result; // Affecte le résultat à la propriété 'pet'
            console.log("got:", PetitionView.pet);
            m.redraw();
        })
    },

    view: function () {
        var pet = PetitionView.pet; // Utilise la propriété 'pet' définie dans l'objet PetitionView

          return m(".petition-page", [
                m("h1", {class: "title"}, pet.properties.nom),
                m("div", {class: "content"}, [
                    m("div", {class:"left"}, [
                        m("img", { src: pet.properties.image, alt: pet.properties.nom, width: "100%"}),
                        m("p", pet.properties.description),
                    ]),
                    m("div", {class: "right"}, [
                        m("div", {style:"padding-bottom:20px;"},"Il y a actuellement "+pet.properties.nbVotants + " signataires, objectif "+ pet.properties.objectif+" !"),
                        m(".sign", [
                            m("a", { class: "button-small", href: "index_petiquik.html#!/petitions", style:"width:100%; text-align:center; font-weight:bold;" }, "Signer la pétition"),
                        ]),
                        m(".tags", {style: "margin-top:10px"}, [
                            m("h3", "Tags"),
                            m("li",pet.properties.tags.join(", "))
                        ]),
                    ])
                ]),
        ]);
    }
};

var Petition = {
    view: function (vnode) {
        if (vnode.attrs.properties.description.length > 200) {
            desc = vnode.attrs.properties.description.substr(0, 200)+"...";
        } else {
            desc = vnode.attrs.properties.description
        }
        return m("li", [
            m(".petition-image", [
                m("img", { src: vnode.attrs.properties.image, alt: vnode.attrs.properties.nom }),
            ]),
            m(".petition-content", [
                m("h3", vnode.attrs.properties.nom),
                m("p", desc),
                m("div.signature-count", vnode.attrs.properties.nbVotants + " signataires"),
            ]),
            m(".sign", [
                m("a", { class: "button-small", href: "index_petiquik.html#!/petition/"+vnode.attrs.key.id }, "Signer"),
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

var ProfilePage = {
    view: function () {
        if (Login.ID) {
            return m("body", [
                m(Header),
                m(ProfileView, { user: Login, petitions: popularPetitions.list }),
                m(Footer)
            ]);
        } else {
            m.route.set("/home");
        }
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

var CreatePage = {
    view: function () {
        if (Login.ID) {
            return m("body", [
                m(Header),
                m(CreateView),
                m(Footer)
            ]);
        } else {
            m.route.set("/home");
        }
    }
};

m.route(document.body, "/home", {
    "/home": HomePage,
    "/petitions": AllPetitionsPage,
    "/profile": ProfilePage,
    "/petition/:id": PetitionPage,
    "/create": CreatePage,
})