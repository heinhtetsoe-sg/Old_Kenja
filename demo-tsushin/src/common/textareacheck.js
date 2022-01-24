function getCursorPos(input) {
    var pos = -1;
    var ta = input[0];
    if (ta.selectionStart) {
        pos = parseInt(ta.selectionStart,10);
    }
        else if (typeof(document.selection) != "undefined") {
        var sel = document.selection.createRange();
        sel.moveStart ('character', -ta.value.length);
        pos = sel.text.length;
    }
    else if (window.getSelection() != "undefined") {
        var sel2 = window.getSelection();
        if (sel2==""){
            pos=0;
        } else {
            var rng = sel2.getRangeAt(0);
            pos = rng.startOffset;
        }
    }
    return pos;
}
function setCursorPos(input, start) {
    var ta = input[0];
    if (ta.setSelectionRange) {
        ta.setSelectionRange(start,start);
    }
    else if (ta.createTextRange) {
        var rng = ta.createTextRange();
        rng.collapse(true);
        rng.moveStart("character", start);
        rng.moveEnd("character", start);
        rng.select();
    }
}
// TEXTAREA入力制限
lineHeadRestrict = "。、.．,，)]}）］｝〕〉＞》」』】’”ゝゞ｡｣､･ﾟ";
lineEndRestrict = "([{（[｛〔〈＜《「『【｢";
//半角カナの確認
hc = "｡｢｣､･ｦｧｨｩｪｫｬｭｮｯｰｱｲｳｴｵｶｷｸｹｺｻｼｽｾｿﾀﾁﾂﾃﾄﾅﾆﾇﾈﾉﾊﾋﾌﾍﾎﾏﾐﾑﾒﾓﾔﾕﾖﾗﾘﾙﾚﾛﾜﾝﾞﾟ";

function hankakuCheck(moji) {
    // 戻り値：　半角：1 全角：2
    if ( hc.indexOf(moji) >=0 ) {
        return 1;
    } else if ( escape(moji).length < 4 ) {
        return 1;
    } else {
        return 2;
    }
}
function lengthCheck(mojiretsu) {
    // 戻り値:半角1,全角2として、文字列全体の長さを返す
    var retlen = 0;
    var maxlen = mojiretsu.length;
    for (var loopcount=0; loopcount< maxlen; loopcount++){
        retlen = retlen + hankakuCheck(mojiretsu.charAt(loopcount));
    }
    return retlen;
}
function splitPos(nyuuryoku,maxlength){
    //入力文字列がmaxlengthより大きい場合に、最初の行として分割する先頭からの文字数を返す。
    //このとき、禁則処理を意識した状態で返すようにする。
    var Str3;
    var temp;
    var curlen=0;
    var retpos=0;
    var icnt=0;
    if (lengthCheck(nyuuryoku)<=maxlength){
        //行末チェックをする
        for (icnt=nyuuryoku.length-1; icnt>=0; icnt--){
            Str3 = nyuuryoku.charAt(icnt);
            if (lineEndRestrict.indexOf(Str3)<0){
                retpos = icnt+1;
                break;
            }
        }
        if (icnt <= 0){
            retpos = nyuuryoku.length;
        }
    } else {
        for(icnt=0; icnt < nyuuryoku.length; icnt++ ){
            Str3 = nyuuryoku.charAt(icnt);
            temp = hankakuCheck(Str3);
            if (curlen+temp > maxlength ){
                if(lineHeadRestrict.indexOf(Str3) < 0 ){
                    for(var icnt2=icnt-1; icnt2 >= 0; icnt2--){
                        Str3 = nyuuryoku.charAt(icnt2);
                        if ( lineEndRestrict.indexOf(Str3) < 0){
                            break;
                        }
                    }
                    if (icnt2<0){
                        retpos = icnt;;
                    } else {
                        retpos = icnt2+1;
                    }
                } else {
                    var headOK = false;
                    for(var icnt3=icnt-1; icnt3 >= 0; icnt3--){
                        Str3 = nyuuryoku.charAt(icnt3);
                        if (headOK){
                            if(lineEndRestrict.indexOf(Str3)<0){
                                break;
                            }
                        } else if( lineHeadRestrict.indexOf(Str3)<0){
                            headOK = true;
                        }
                    }
                    if (icnt3 <0 ){
                        retpos = icnt;
                    } else {
                        retpos = icnt3+1;
                    }
                }
                break;
            }
            curlen = curlen+temp;
        }
    }
    return retpos;
}
function splitPosLast(nyuuryoku,maxlength){
    //最終行は行末禁則をしない。その場合のあふれているときでも最大の文字数を返す。
    var Str3;
    var temp;
    var curlen=0;
    var retpos=0;
    var icnt=0;
    if (lengthCheck(nyuuryoku)<=maxlength){
        retpos = nyuuryoku.length;
    } else {
        for(icnt=0; icnt < nyuuryoku.length; icnt++ ){
            Str3 = nyuuryoku.charAt(icnt);
            temp = hankakuCheck(Str3);
            if (curlen+temp > maxlength ){
                retpos = icnt;
                break;
            }
            curlen = curlen+temp;
        }
    }
    return retpos;
}
function check(e,maxlength,maxrow,stats) {
    // remember cursor position
    var cursorpos = getCursorPos($(e.target));
    var curline=0, inlinepos=0, parsepos=0;
    var oldcursorpos = cursorpos;
    cursorposFound = false;
    var templen = 0;
    var Str1;
    Str1 = $(e.target).val();
    // Excelから複数行Cut&Pasteするときにダブルクォートでくくられる部分に対応　start
    templen = Str1.length;
    if (templen>2) {
        if(Str1.charAt(templen-1)=="\n" && Str1.charAt(templen-2)=="\"" && Str1.charAt(0)=="\"" ) {
            Str = Str1.substring(1,templen-2);
            Str = Str.replace(/\"\"/g,"\"");
        } else {
            Str = Str1;
        }
    } else {
        Str = Str1;
    }
    // Excelから複数行Cut&Pasteするときにダブルクォートでくくられる部分に対応 end
    Str = Str.replace(/(\r\n|\r|\n)/g,"\n");
    error=0;
    StrAry = Str.split(/\n/);
    lines = StrAry.length;
    if( lines > maxrow ){
        error=1;
        lines=maxrow;
    }
    if (error == 0 ) {
        for(i=0; i<lines;i++){
            if (!cursorposFound && parsepos + StrAry[i].length >= cursorpos ){
                cursorposFound = true;
                curline = i;
                inlinepos = cursorpos-parsepos;
                break;
            }
            parsepos = parsepos+StrAry[i].length+1;  //見つからないときにその行の文字数+改行分を加える。
        }
        for(i=lines-2; i>=0; i--){
            if(StrAry[i+1].length>0 && lengthCheck(StrAry[i])+hankakuCheck(StrAry[i+1].charAt(0)) > maxlength && i < lines-1 ){ //桁あふれまたは、最大の場合次の行とマージする。
                if(curline == i+1){
                    curline = curline-1;
                    inlinepos = inlinepos + StrAry[i].length;
                    cursorpos = cursorpos -1;
                }
                StrAry[i] = StrAry[i]+StrAry[i+1];
                for (tempi=i+1; tempi<lines-1;tempi++){
                    StrAry[tempi] = StrAry[tempi+1];
                }
                lines= lines-1;
            }
        }
        for(i=0; i<lines-1;i++){
            if(StrAry[i].length>0 && lineEndRestrict.indexOf(StrAry[i].charAt(StrAry[i].length-1))>=0 && StrAry[i+1].length>0){ //最後の文字が行末禁則の場合、マージする。
                if(curline >= i+1){
                    curline = curline-1;
                    inlinepos = inlinepos + StrAry[i].length;
                    cursorpos = cursorpos -1;
                }
                StrAry[i] = StrAry[i]+StrAry[i+1];
                for (tempi=i+1; tempi<lines-1;tempi++){
                    StrAry[tempi] = StrAry[tempi+1];
                }
                lines= lines-1;

            }
        }
        for(i=1; i<lines;i++){ //先頭文字が行頭禁則の場合、前の行とくっつける。
            if(StrAry[i].length>0 && lineHeadRestrict.indexOf(StrAry[i].charAt(0))>=0){
                if (i == curline ){
                    curline = curline-1;
                    inlinepos = inlinepos + StrAry[i-1].length;
                    cursorpos = cursorpos -1;
                }
                StrAry[i-1] = StrAry[i-1]+StrAry[i];
                for (tempi=i; tempi<lines-1;tempi++){
                    StrAry[tempi] = StrAry[tempi+1];
                }
                lines= lines-1;
            }
        }
        var outLen = new Array(maxrow); //実質の行の中の長さ
        var outStr = new Array(maxrow); //実質の行の値
        k=0; //実質の行－１
        for(i=0;i<lines;i++){  //入力行の処理
            if( k >= maxrow ) { //実質の行が最大行を超えた
               error=2;
               errorline=i;
               break;
            }
            StrAry[i] = StrAry[i].replace("/\r/g","");
            var templen = StrAry[i].length;
            var inputStr = StrAry[i];
            var splitpoint=0;
            parsepos = 0;
            do {
                if (k== maxrow-1){
                    splitpoint = splitPosLast(inputStr,maxlength);
                } else {
                    splitpoint = splitPos(inputStr,maxlength);
                }
//              console.log("k="+k+"  splitpoint="+splitpoint);
                parsepos = parsepos + splitpoint;
                templen = templen - splitpoint;
                outStr[k] = inputStr.substr(0,splitpoint);
                outLen[k] = lengthCheck(outStr[k]);
                if (templen == 0){
                    if( i < lines-1 ) {
                        outStr[k] = outStr[k]+"\n";
                    }
                } else {
                    if (i == curline && parsepos < inlinepos ){
                        cursorpos = cursorpos + 1;
                        if ( k == maxrow-1){
                            cursorpos = cursorpos + parsepos - inlinepos;
                        }
                    }
                    if( k < maxrow-1 ){
                        outStr[k] = outStr[k]+"\n";
                    }
                    inputStr = inputStr.substr(splitpoint);
                }
                k= k+1;
                if (k==maxrow){
                    break;
                }
            } while (templen > 0);
        }  //入力の次の行へ
        if ( k> maxrow && error !==2 ) {
             error=2;
             errorline=i;
        }
    }
    statustext = "";  // debug
    if (error==1) {
       outStr1 = "";
       for(i=0; i<maxrow; i++) {
          if ( i==maxrow-1) {
              outStr1 = outStr1 + StrAry[i];
              hcleft = maxlength - lengthCheck(StrAry[i]);
              zcleft = Math.floor(hcleft/2);
              statustext = statustext + "<b>"+(i+1) + "</b>行目 残り全角<b>" +zcleft + "</b>/半角<b>" + hcleft + "</b>文字";  //debug
          } else {
              outStr1 = outStr1 + StrAry[i]+"\n";
          }
       }
       $(e.target).val(outStr1);
    } else if (error==2) {
       outStr1 = "";
       for(i=0; i<maxrow; i++) {
          outStr1 = outStr1+outStr[i];
          if (i == maxrow-1 ) {
              hcleft = maxlength - outLen[i];
              zcleft = Math.floor(hcleft/2);
              statustext = statustext + "<b>"+(i+1) + "</b>行目 残り全角<b>" +zcleft + "</b>/半角<b>" + hcleft + "</b>文字";  //debug
          }
       }
       $(e.target).val(outStr1);
    } else {
       outStr1 = "";
       if ( k >= maxrow ) {
          lll = maxrow;
       } else {
          lll = k;
       }
       for(i=0; i<lll; i++) {
          outStr1 = outStr1+outStr[i];
          if (i == lll-1 ) {
              hcleft = maxlength - outLen[i];
              zcleft = Math.floor(hcleft/2);
              statustext = statustext + "<b>"+(i+1) + "</b>行目 残り全角<b>" +zcleft + "</b>/半角<b>" + hcleft + "</b>文字";  //debug
          }
       }
       $(e.target).val(outStr1);
    }
    setCursorPos($(e.target),cursorpos);
    statustext = statustext;
    document.getElementById(stats).innerHTML = statustext;     //debug
    delete outStr;
    delete outLen;
}
(function () {
    // window.console が未定義なら、オブジェクトにする
    if (typeof window.console === "undefined") {
         window.console = {}
    }
    // window.console.log が function でないならば、空の function を代入する
    if (typeof window.console.log !== "function") {
         window.console.log = function () {}
    }
})();
/*! Japanese input change plugin for jQuery.
    https://github.com/hnakamur/jquery.japanese-input-change
    (c) 2014 Hiroaki Nakamura
    MIT License
 */
(function($, undefined) {
  $.fn.japaneseInputChange = function(selector, handler) {
    var isComposing = false,
        isReadytoStopCompose = false,
        isIEMidCompose = false,
        isStopCompose = false,
        isIMEbox = false,
        isCancelIME = false,
        oldVal;
    var isFocus;
    // Microsoftのブラウザかどうか
    var isMsBrowser = false;
    var isChrome = false;
    var atokFirsttime = false;
    var isATOK;
    if (window.fep==2) {
        isATOK=true;
    } else {
        isATOK=false;
    }

    // IE9かどうか
    var isIE9 = false;
    // ブラウザ判定
    // EdgeをMicrosoft系のブラウザとして入れているが、
    // WebKitと動作が異なる部分は修正すべきバグと
    // http://blogs.windows.com/msedgedev/2015/06/17/building-a-more-interoperable-web-with-microsoft-edge/
    // でも述べている為、将来的には削除する必要があるかも
    var ua = window.navigator.userAgent.toLowerCase();
    if (ua.indexOf('msie') > -1 ||
            ua.indexOf('trident') > -1 ||
            ua.indexOf('edge') > -1) {
                isMsBrowser = true;
        if (ua.indexOf('msie 9.0') > -1) {
            isIE9 = true;
        }
    } else if ( ua.indexOf('chrome') > -1 ) {
        isChrome = true;
    }
    if (handler === undefined) {
      handler = selector;
      selector = null;
    }

    return this.on('focus', selector, function(e) {
         oldVal = $(e.target).val();
         isFocus = true;
         $(e.target).trigger('input');
    })
    .on('compositionstart', selector, function(e) {
      isComposing = true;
      isReadytoStopCompose = false;

    })
    .on('compositionend', selector, function(e) {
        isComposing = false;
        if (isMsBrowser) {
                if (isStopCompose){
                    isIEMidCompose = true;
                    isStopCompose = false;
                    isReadytoStopCompose = false;
                    $(e.target).trigger('input');
                } else {
                    if(isIEMidCompose || isCancelIME ){
                        isIEMidCompose = false;
                    } else {
                        $(e.target).trigger('input');
                    }
                }
        }
        if (isChrome) {
            $(e.target).trigger('input');
        }
    })
    .on('keydown', selector, function(e) { //UNDO無効化
        switch (e.keyCode){
        case 90:
            if(e.ctrlKey){
                return false;
            }
        default:
            break;
        }
        if (isMsBrowser ) {
            if (isComposing) {
                isCancelIME = false;
                switch (e.keyCode){
                case 229:
                    switch (e.key){
                    case "Convert":
                    case "Nonconvert":
                    case "Spacebar":
                        if ( isReadytoStopCompose ){
                            isIMEbox = true;
                        } else {
                            isReadytoStopCompose = true;
                        }
                        break;
                    case "Backspace":
                    case "Shift":
                    case "Control":
                    case "Alt":
                    case "Unidentified":
                    case "Right":
                    case "Left":
                    case "Down":
                    case "Up":
                    case "Win":
                    case "Delete":
                    case "Numlock":
                    case "Home":
                    case "End":
                    case "Pageup":
                    case "Pagedown":
                        break;
                    case "Esc":
                        isCancelIME = true;
                    case "Enter":
                        if (isReadytoStopCompose){
                            isReadytoStopCompose = false;
                            isComposing = false;
                            isIMEbox = false;
                        }
                        break;
                    case "0":
                    case "1":
                    case "2":
                    case "3":
                    case "4":
                    case "5":
                    case "6":
                    case "7":
                    case "8":
                    case "9":
                        if ( isIMEbox ){
                            isIMEbox = false;
                            break;
                        }
                    default:
                        if (isReadytoStopCompose){
                            isReadytoStopCompose = false
                            isStopCompose=true;
                            isIMEbox = false;
                            $(e.target).trigger('compositionend');
                        }
                        break;
                    }
                default:
                    break;
                }
            }
        }
    })
    .on('input', selector, function(e) {
        var $el = $(e.target),
        val;
      if (!isComposing) {
          val = $el.val();
        if (val == oldVal) {
          if ( isFocus ) {
             isFocus = false;
             handler.call($el,e);
             oldVal =$el.val();   // エラーの場合に内容が書き換わることから、ここで再取得する。
          }
        } else {
          isFocus = false;
          if( isATOK && isIEMidCompose ){
              isIEMidCompose = false;
          } else {
              handler.call($el, e);
              oldVal = $el.val();     // エラーの場合に内容が書き換わることから、ここで再取得する。
          }
        }
      } else if (isIEMidCompose){
          handler.call($el, e);
          oldVal = $el.val();
      }
    });
  };
}(jQuery));
