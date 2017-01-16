var iishLogo = document.getElementById('iishLogo');
var metadata = document.getElementById('metadata');

var containers = document.getElementsByClassName('container');
var imageContainer = document.getElementById('imageContainer');
var metadataContainer = document.getElementById('metadataContainer');

var mainImages = imageContainer.getElementsByTagName('img');
var metadataDls = metadataContainer.getElementsByTagName('dl');

var setMaxHeight = function () {
    var maxHeight = window.innerHeight;
    for (var i = 0; i < containers.length; i++) {
        containers[i].setAttribute('style', 'height: ' + maxHeight + 'px; max-height: ' + maxHeight + 'px;');
    }
};

var centerMainImage = function (mainImage) {
    var containerWidth = Math.floor((mainImage.parentElement.offsetWidth) / 2);
    var containerHeight = Math.floor((mainImage.parentElement.offsetHeight) / 2);

    var centerWidth = Math.floor(mainImage.offsetWidth / 2);
    var centerHeight = Math.floor(mainImage.offsetHeight / 2);

    var width = containerWidth - centerWidth;
    var height = containerHeight - centerHeight;

    mainImage.setAttribute('style', 'top: ' + height + 'px; left: ' + width + 'px;');
};

var setLogo = function () {
    var height = Math.ceil(iishLogo.height / 2);
    metadata.setAttribute('style', 'margin-top: -' + height + 'px; padding-top: ' + (height + 10) + 'px;');
};

var newSlide = function () {
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if ((xmlHttp.readyState == XMLHttpRequest.DONE) && (xmlHttp.status == 200)) {
            var hiddenImg = imageContainer.querySelector('img.hidden');
            var hiddenDl = metadataContainer.querySelector('dl.hidden');

            if ((hiddenImg != null) && (hiddenDl != null)) {
                var slide = JSON.parse(xmlHttp.responseText);

                hiddenDl.innerHTML = '';
                for (var key in slide.metadata) {
                    if (slide.metadata.hasOwnProperty(key)) {
                        var dt = document.createElement('dt');
                        dt.appendChild(document.createTextNode(key));
                        hiddenDl.appendChild(dt);

                        for (var i = 0; i < slide.metadata[key].length; i++) {
                            var dd = document.createElement('dd');
                            dd.appendChild(document.createTextNode(slide.metadata[key][i]));
                            hiddenDl.appendChild(dd);
                        }
                    }
                }

                hiddenImg.setAttribute('src', '/image?barcode=' + slide.barcode);
                hiddenImg.onload = function () {
                    centerMainImage(this);

                    for (var i = 0; i < mainImages.length; i++) {
                        mainImages[i].classList.toggle('hidden');
                    }

                    for (var i = 0; i < metadataDls.length; i++) {
                        metadataDls[i].classList.toggle('hidden');
                    }
                };
            }
        }
    };

    xmlHttp.open('GET', '/nextSlide?r=' + Math.random(), true);
    xmlHttp.send();
};

setMaxHeight();
setLogo();
newSlide();

window.onresize = function () {
    setMaxHeight();
    setLogo();

    for (var i = 0; i < mainImages.length; i++) {
        centerMainImage(mainImages[i]);
    }
};

iishLogo.onload = function () {
    setLogo();
};

var body = document.getElementsByTagName('body')[0];
var timeout = parseInt(body.dataset.timeout);
if (timeout > 0) {
    window.setInterval(function () {
        newSlide();
    }, timeout);
}

iishLogo.onclick = function () {
    if (!document.fullscreenElement && !document.mozFullScreenElement && !document.webkitFullscreenElement && !document.msFullscreenElement) {
        if (document.documentElement.requestFullscreen) {
            document.documentElement.requestFullscreen();
        }
        else if (document.documentElement.msRequestFullscreen) {
            document.documentElement.msRequestFullscreen();
        }
        else if (document.documentElement.mozRequestFullScreen) {
            document.documentElement.mozRequestFullScreen();
        }
        else if (document.documentElement.webkitRequestFullscreen) {
            document.documentElement.webkitRequestFullscreen(Element.ALLOW_KEYBOARD_INPUT);
        }
        iishLogo.requestPointerLock();
    }
    else {
        if (document.exitFullscreen) {
            document.exitFullscreen();
        }
        else if (document.msExitFullscreen) {
            document.msExitFullscreen();
        }
        else if (document.mozCancelFullScreen) {
            document.mozCancelFullScreen();
        }
        else if (document.webkitExitFullscreen) {
            document.webkitExitFullscreen();
        }
        document.exitPointerLock();
    }
};