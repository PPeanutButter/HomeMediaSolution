const path_list = [];

function goNext(name) {
    path_list.push(name);
    getFileList();
}

function goBack() {
    if (path_list.length > 0) {
        path_list.pop()
        getFileList();
    } else openSnackbar("不能再返回了");
}

function onItemClick(name, type) {
    console.log(name);
    if (type === "Directory")
        goNext(name);
}

Array.prototype.toString = function() {
  if(this.length === 0) {
    return '/';
  }
  return this.join('/');
};

Array.prototype.toPath = function() {
  if(this.length === 0) {
    return '/';
  }
  return '/'+this.join('/') + '/';
};

function getCookie(name){
    const cookie_str = document.cookie;//获取cookie字符串
    const cookie_array = cookie_str.split("; ");//分割
    //遍历匹配
    for (let i = 0; i < cookie_array.length; i++) {
        const arr = cookie_array[i].split("=");
        if (arr[0] === name){
            return arr[1];
        }
    }
    return "";
}

function getFileList() {
    $.get("/getFileList?path=" + path_list.toPath(), function (data) {
        $('#dir-panel').empty();
        $('#file-panel').empty();
        let footer = $('.footer');
        footer.empty();
        $('.mdc-top-app-bar__title').text(path_list.toString())
        for (const list of eval(data)) {
            if (list.watched !== "watched"){
                if (list.type === "Directory"){
                    $('div#dir-panel').append(String.format(directory_html_data, list.name, list.title))
                }
                else if (list.type === "File") {
                    let __1 = list.name
                    let _0 = __1.substring(__1.lastIndexOf('/')+1)
                    let _1 = list.mime_type
                    let _2 = encodeURIComponent(__1)
                    let _3 = window.location.host + "/getFile/" + _0 + "?path=" + _2 +"&token=" + getCookie('token')
                    let _4 = _2
                    let _5 = list.bookmark_state
                    let _6 = list.watched
                    $('div#file-panel').append(String.format(file_html_data, _0, _1, _2, _3, _4, _5, _6));
                } else if (list.type === "Attach") {
                    let __1 = list.name
                    let _0 = __1.substring(__1.lastIndexOf('/')+1)
                    let _2 = encodeURIComponent(__1)
                    let _3 = window.location.host + "/getFile/" + _0 + "?path=" + _2
                    const p = document.createElement("li");
                    const a = document.createElement("a");
                    a.href="http://"+_3;
                    a.appendChild(document.createTextNode(_0));
                    p.appendChild(a);
                    footer.append(p);
                }
            }
        }
    });
}

function onDialogButtonClick(url, type) {
    console.log(url)
    if (type === "copy") {
        const clipboard = new ClipboardJS('.dialog-copy', {
            text: function (trigger) {
                console.log(trigger);
                return url;
            }
        });
        clipboard.on('success', function (e) {
            openSnackbar("复制成功");
            e.clearSelection();
            clipboard.destroy();
        });
        clipboard.on('error', function (e) {
            openSnackbar("复制失败：" + e + "，请手动复制");
            clipboard.destroy();
        });
    } else if (type === "download") {
        window.open("http://" + url);
    } else if (type === "play") {
        window.open("potplayer://http://" + url);
    } else if (type === "bookmark") {
        $.get("/toggleBookmark?path=" + url, function (data) {
            openSnackbar(data)
            getFileList()
        });
    }
}

String.format = function () {
    if (arguments.length === 0)
        return null;
    let str = arguments[0];
    for (let i = 1; i < arguments.length; i++) {
        const re = new RegExp('\\{' + (i - 1) + '\\}', 'gm');
        str = str.replace(re, arguments[i]);
    }
    return str;
};

function openSnackbar(error_msg) {
    $("div.mdc-snackbar__label").text(error_msg);
    const snackbar = new mdc.snackbar.MDCSnackbar(document.querySelector('.mdc-snackbar'));
    snackbar.open();
}

getFileList()