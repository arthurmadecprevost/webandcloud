var Login = {
    name: "",
    email: "",
    ID: "",
    picture: "",
    creationDate: "",
    handleCredential: function (response) {
        // decodeJwtResponse() is a custom function defined by you
        // to decode the credential response.
        const responsePayload = jwt_decode(response.credential);

        //DiceGame.name = responsePayload.name
        Login.name = responsePayload.name
        Login.email = responsePayload.email
        Login.ID = responsePayload.sub
        Login.credential = response.credential
        //Login.ID = response.credential
        Login.picture = responsePayload.picture
        fullName = Login.name;

        var data = {'body': fullName};
        
        m.request({
            method: "GET",
            url: "_ah/api/petiQuik/v1/checkCreatedUser?access_token="+Login.credential,
            params: data
        }).then(function (result) {
            Login.creationDate = result.properties.creationDate;
            m.redraw()
        })
        // external event
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
                        m("li", m("a", { href: "index_petiquik.html#!/petitions/1" }, "Pétitions")),
                        m("li", m("a", { href: "index_petiquik.html#!/create" }, "Nouvelle pétition")),
                        m("li", m("a", { href: "index_petiquik.html#!/searchInput" }, "Rechercher une pétition")),
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
                        m("li", m("a", { href: "index_petiquik.html#!/petitions/1" }, "Pétitions")),
                        m("li", m("a", { href: "index_petiquik.html#!/searchInput" }, "Rechercher une pétition")),
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
                m("a", { class: "button-big", href: "index_petiquik.html#!/petitions/1" }, "Voir toutes les pétitions"),
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
            // m.redraw();
        })
    },
};

var petitionPage = {
    list: [],
    loadList: function(pageId) {
        list = [];
        m.redraw();
        if (pageId > 0) {
            return m.request({
                method: "GET",
                url: "_ah/api/petiQuik/v1/getPetitions/"+pageId
            }).then(function (result) {
                petitionPage.list = result.items;
            })
        }
    }
}

var popularPetitions = {
    list: [],
    loadList: function () {
        return m.request({
            method: "GET",
            url: "_ah/api/petiQuik/v1/top4/"
        }).then(function (result) {
            popularPetitions.list = result.items;
            // m.redraw();
        })
    },
}

var myPetitions = {
    list: [],
    loadList: function () {
        return m.request({
            method: "GET",
            url: "_ah/api/petiQuik/v1/mesPetitions"+'?access_token='+encodeURIComponent(Login.credential)
        }).then(function (result) {
            myPetitions.list = result.items;
            // m.redraw();
        })
    },
}

var signedPetitions = {
    list: [],
    loadList: function () {
        return m.request({
            method: "GET",
            url: "_ah/api/petiQuik/v1/mesSignatures"+'?access_token='+encodeURIComponent(Login.credential),
        }).then(function (result) {
            signedPetitions.list = result.items;
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

var PaginatedPetitionView = {
    onupdate: function(vnode) {
        if (vnode.attrs.pageId !== vnode.state.previousPageId) {
            vnode.state.previousPageId = vnode.attrs.pageId;
            location.reload();
        }
    },

    oninit: function(vnode) {
        vnode.state.previousPageId = vnode.attrs.pageId;
        petitionPage.loadList(vnode.attrs.pageId);
    },

    view: function(vnode) {
        if (petitionPage.list.length > 0) { 
            var pagination = [
                m("a", {
                    class: "button-small",
                    href: "index_petiquik.html#!/petitions/" + (parseInt(vnode.attrs.pageId, 10) - 1)
                }, "Page précédente"),
                m("p", {
                    "style": "margin:20px"
                }, "Page " + vnode.attrs.pageId),
                m("a", {
                    class: "button-small",
                    href: "index_petiquik.html#!/petitions/" + (parseInt(vnode.attrs.pageId, 10) + 1)
                }, "Page suivante"),
            ];

            if (vnode.attrs.pageId == 1 && petitionPage.list.length < 50) {
                pagination = [  
                    m("p", {
                        "style": "margin:20px"
                    }, "Page " + vnode.attrs.pageId),
                ];
            }

            if (vnode.attrs.pageId > 1 && petitionPage.list.length < 50) {
                pagination = [
                    m("a", {
                        class: "button-small",
                        href: "index_petiquik.html#!/petitions/" + (parseInt(vnode.attrs.pageId, 10) - 1)
                    }, "Page précédente"),
                    m("p", {
                        "style": "margin:20px"
                    }, "Page " + vnode.attrs.pageId)
                ];
            }

            if (vnode.attrs.pageId == 1 && petitionPage.list.length == 50) {
                pagination = [
                    m("p", {
                        "style": "margin:20px"
                    }, "Page " + vnode.attrs.pageId),
                    m("a", {
                        class: "button-small",
                        href: "index_petiquik.html#!/petitions/" + (parseInt(vnode.attrs.pageId, 10) + 1)
                    }, "Page suivante"),
                ];
            }

            return m(".petitions", [
                m("h2", "Nos pétitions (page " + vnode.attrs.pageId + ")"),
                m("ul", petitionPage.list.map(function(petition) {
                    return m(Petition, petition);
                })),
                m(".pagination", pagination),
            ]);
        } else {
            return m(".petitions", [
                m("h1", "Ça arrive !"),
                m("p", "Nous avons assignés tous nos nains de recherche à votre requête, ça arrive !"),
                m("img", {src: "https://upload.wikimedia.org/wikipedia/commons/d/d0/7_garden_gnomes.jpg", alt:"Image de nain"}),
                m("p", "* Photo non contractuelle"),
            ]); 
        }
    },
};

var searchPetition = {
    list: [],
    loadList: function(type, text, id) {
        list = [];
        m.redraw();
        var data = {
            'type': type,
            'searchText': text,
            'page': id,
        };
        if (id > 0) {
            return m.request({
                method: "GET",
                url: "_ah/api/petiQuik/v1/searchPetition",
                params: data,
            }).then(function (result) {
                searchPetition.list = result.items;
            })
        }
    }
}

var SearchResult = {
    onupdate: function(vnode) {
        if (vnode.attrs.id !== vnode.state.previousSearchId) {
            vnode.state.previousSearchId = vnode.attrs.id;
            location.reload();
        }
    },

    oninit: function(vnode) {
        vnode.state.previousSearchId = vnode.attrs.id;
        searchPetition.loadList(vnode.attrs.type,vnode.attrs.text,vnode.attrs.id);
    },

    view: function(vnode) {
        var type = vnode.attrs.type;
        var text = vnode.attrs.text;
        var id = vnode.attrs.id;

        if (searchPetition.list.length > 0) { 
            var pagination = [
                m("a", {
                    class: "button-small",
                    href: "index_petiquik.html#!/search/" + type + "/" + text + "/" + (parseInt(id, 10) - 1)
                }, "Page précédente"),
                m("p", {
                    "style": "margin:20px"
                }, "Page " + id),
                m("a", {
                    class: "button-small",
                    href: "index_petiquik.html#!/search/" + type + "/" + text + "/" + (parseInt(id, 10) + 1)
                }, "Page suivante"),
            ];

            if (id == 1 && searchPetition.list.length < 50) {
                pagination = [  
                    m("p", {
                        "style": "margin:20px"
                    }, "Page " + id),
                ];
            }

            if (id > 1 && searchPetition.list.length < 50) {
                pagination = [
                    m("a", {
                        class: "button-small",
                        href: "index_petiquik.html#!/search/" + type + "/" + text + "/" + (parseInt(id, 10) - 1)
                    }, "Page précédente"),
                    m("p", {
                        "style": "margin:20px"
                    }, "Page " + id)
                ];
            }

            if (id == 1 && searchPetition.list.length == 50) {
                pagination = [
                    m("p", {
                        "style": "margin:20px"
                    }, "Page " + id),
                    m("a", {
                        class: "button-small",
                        href: "index_petiquik.html#!/search/" + type + "/" + text + "/" + (parseInt(id, 10) + 1)
                    }, "Page suivante"),
                ];
            }

            return m(".petitions", [
                m("h2", "Votre recherche (page " + id + ")"),
                m("ul", searchPetition.list.map(function(petition) {
                    return m(Petition, petition);
                })),
                m(".pagination", pagination),
            ]);
        } else {
            return m(".petitions", [
                m("h1", "Ça arrive !"),
                m("p", "Nous avons assignés tous nos nains de recherche à votre requête, ça arrive !"),
                m("img", {src: "https://upload.wikimedia.org/wikipedia/commons/d/d0/7_garden_gnomes.jpg", alt:"Image de nain"}),
                m("p", "* Photo non contractuelle"),
            ]); 
        }
    }
}

const ProfileView = {
    view: function (vnode) {
        const user = vnode.attrs.user;

        return m('.div', [
            m('.profile-card', [
                m('.profile-image', [
                    m('img', { src: user.picture })
                ]),
                m('.profile-info', [
                    m('h2.profile-name', user.name),
                    m('p.profile-date', 'Membre depuis le ' + user.creationDate),
                ]),
            ])
        ]);
    }
};

var CreateView = {
    name: "",
    description: "",
    objective: 1,
    picture: "",
    tags: "",
  
    submitForm: function () {
        var pet = {
            nom: CreateView.name,
            description: CreateView.description,
            image: CreateView.picture,
            tags: CreateView.tags,
            objectif: CreateView.objective,
        };
        return m.request({
            method: "POST",
            url: "_ah/api/petiQuik/v1/addPetition"+'?access_token='+Login.credential,
            params: pet,
        })
        .then(function(result) {
            m.route.set("/petition/:id", {id: result.key.id})
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
            value: CreateView.tags,
            oninput: function (e) { CreateView.tags = e.target.value; },
            required: true,
            }),
            
            m("button[type=submit]",{class: "create-button", style: "margin:20px;"} ,"Créer la pétition")
      ]);
    }
  };

  var MyPetitionsView = {
    oninit: function(vnode) {
        myPetitions.loadList(vnode.attrs.myPetsId);
    },
    view: function() {
        return m('div', [
            m('h3.profile-petitions-header', 'Mes pétitions'),
            m('ul.profile-petitions-list', myPetitions.list.map(function (petition) {
                return m('li', [
                    m('a', { href: "index_petiquik.html#!/petition/"+petition.key.id }, petition.properties.nom)
                ])
           }))
       ]);
    }
  }

  var SignedPetitionsView = {
    oninit: function(vnode) {
        signedPetitions.loadList(vnode.attrs.signedPetsId);
    },
    view: function() {
        var petitions = signedPetitions.list;

        return m('div', [
            m('h3.profile-petitions-header', 'Pétitions signées'),
            m('ul.profile-petitions-list', petitions.map(function (petition) {
                return m('li', [
                    m('a', { href: "index_petiquik.html#!/petition/"+petition.key.id }, petition.properties.nom)
                ])           
            }))
       ]);
    }
  }

var PetitionView = {
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
          "nomCreateur":"",
        }
    },

    oninit: function (vnode) {
        if (vnode.attrs.id != PetitionView.pet.key.id) {
            pet = {
                "key": {
                  "id": "",
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
                  "nomCreateur":"",
                }
            };            
        };
        return m.request({
            method: "GET",
            url: "_ah/api/petiQuik/v1/getPetition/"+vnode.attrs.id,
        }).then(function (result) {
            PetitionView.pet = result; // Affecte le résultat à la propriété 'pet'
            m.redraw();
        })
    },

    sign: function () {
        if (PetitionView.pet.key.id != "") {
            var pet = {
                id: PetitionView.pet.key.id,
                nom: PetitionView.pet.properties.nom,
                description: PetitionView.pet.properties.description,
                image: PetitionView.pet.properties.image,
                tags: PetitionView.pet.properties.tags,
                objectif: PetitionView.pet.properties.objectif
            };

            var oldCounter;

            return m.request({
                method: "PUT",
                url: "_ah/api/petiQuik/v1/signPetition/"+Login.ID+"/"+PetitionView.pet.key.id,
            })
            .then(function(result) {
                PetitionView.pet.properties.nbVotants = result.properties.nbVotants;
            })
        }       
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
                        Login.credential ? m(".sign", [
                            m("a", { class: "button-small", onclick: PetitionView.sign, style: "width:100%; text-align:center; font-weight:bold;" }, "Signer la pétition"),
                        ]) : m("p", "Connectez-vous pour signer la pétition."),
                        m("p", {style: "text-align: center;margin: 40px;"}, "Publié par: "+pet.properties.nomCreateur),
                        m(".tags", {style: "margin-top:10px"}, [
                            m("h3", "Tags"),
                            m("li", {style: "max-width: 350px;"},pet.properties.tags.join(", "))
                        ]),
                    ])
                ]),
        ]);
    }
};

var SearchView = {
    searchText: "",
    searchType: "1",
  
    submitForm: function () {
        var search =  {
            text: SearchView.searchText,
            type: SearchView.searchType,
        };
        console.log(search);
        m.route.set("/search/:type/:text/:id", {type: search.type, text: search.text, id: "1"});
        SearchView.searchText = "";
        SearchView.searchType = "1";
    },
  
    view: function () {
      return m("form", {
        onsubmit: function (e) {
          e.preventDefault();
          SearchView.submitForm();
        },
        class:"create-form"}, [

            m("h1", "Rechercher une pétition"),
            m("input[type=text]", {
            value: SearchView.searchText,
            oninput: function (e) { SearchView.searchText = e.target.value; },
            required: true
            }),

            m("label",{for:"pet-select"}, "Recherchez par"),
            m("select", {
                name:"pets", 
                id:"pet-select",
                oninput: function (e) { 
                    SearchView.searchType = e.target.value; 
                },
                required: true
            }, [
                m("option", {value: "1"}, "Tag"),
                m("option", {value: "2"}, "Nom"),
            ]),
            m("button[type=submit]",{class: "create-button", style: "margin:20px;"} ,"Rechercher une pétition")
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
    view: function (vnode) {
        var pageId = vnode.attrs.pageId;
        
        return m("body", [
            m(Header),
            m(PaginatedPetitionView, {pageId : pageId}),
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
                m(".petitions", [
                    m(MyPetitionsView),
                    m(SignedPetitionsView),
                ]),
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

var SearchInputPage = {
    view: function() {
        return m("body", [
            m(Header),
            m(SearchView),
            m(Footer)
        ])
    }
};

var SearchResultPage = {
    view: function(vnode) {
        var typeSearch = vnode.attrs.type; // Récupère l'ID de la pétition depuis les attributs du vnode
        var textSearch = vnode.attrs.text; // Récupère l'ID de la pétition depuis les attributs du vnode
        var idPagination = vnode.attrs.id; // Récupère l'ID de la pétition depuis les attributs du vnode

        return m("body", [
            m(Header),
            m(SearchResult, {
                type: typeSearch,
                text: textSearch,
                id: idPagination
            }),
            m(Footer),
        ])
    }
}

m.route(document.body, "/home", {
    "/home": HomePage,
    "/petitions/:pageId": AllPetitionsPage,
    "/profile": ProfilePage,
    "/petition/:id": PetitionPage,
    "/create": CreatePage,
    "/searchInput": SearchInputPage,
    "/search/:type/:text/:id": SearchResultPage,
})