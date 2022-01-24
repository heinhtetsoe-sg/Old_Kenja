function btn_submit(cmd, gzip, gadd) {
  if (cmd == "delete") {
    if (!confirm("{rval MSG103}")) return false;
  }
  if (cmd == "changeApp" || cmd == "addnew") {
    document.forms[0].EXAMNO.value = "";
    document.forms[0].SEARCH_EXAMNO.value = "";
  }
  if (cmd == "reset" && !confirm("{rval MSG106}")) {
    return true;
  }
  if (cmd == "back1" || cmd == "next1") {
    if (document.forms[0].SEARCH_EXAMNO.value == "") {
      alert("{rval MSG301}\n( 受験番号 )");
      return true;
    }
    if (
      vflg == true ||
      gzip != document.forms[0].ZIPCD.value ||
      gadd != document.forms[0].ADDRESS1.value
    ) {
      if (!confirm("{rval MSG108}")) {
        return true;
      }
    }
  }
  if (cmd == "reference" || cmd == "reference2") {
    if (document.forms[0].SEARCH_EXAMNO.value == "" && cmd == "reference") {
      alert("{rval MSG301}\n( 受験番号 )");
      return true;
    }
    if (
      vflg == true ||
      gzip != document.forms[0].ZIPCD.value ||
      gadd != document.forms[0].ADDRESS1.value
    ) {
      if (!confirm("{rval MSG108}")) {
        return true;
      }
    }
  }

  if (cmd == "disp_clear") {
    for (i = 0; i < document.forms[0].elements.length; i++) {
      if (
        document.forms[0].elements[i].type == "select-one" ||
        document.forms[0].elements[i].type == "text" ||
        document.forms[0].elements[i].type == "checkbox"
      ) {
        if (document.forms[0].elements[i].type == "select-one") {
          document.forms[0].elements[i].value =
            document.forms[0].elements[i].options[0].value;
        } else if (document.forms[0].elements[i].type == "checkbox") {
          document.forms[0].elements[i].checked = false;
        } else {
          document.forms[0].elements[i].value = "";
        }
      }
    }
    outputLAYER("FINSCHOOLNAME_ID", "");
    outputLAYER("label_priName1", "");
    outputLAYER("label_priClassName1", "");
    outputLAYER("label_priName2", "");
    outputLAYER("label_priClassName2", "");
    outputLAYER("label_priName3", "");
    outputLAYER("label_priClassName3", "");
    return false;
  }

  document.forms[0].cmd.value = cmd;
  document.forms[0].submit();
  return false;
}

function toTelNo(checkString) {
  var newString = "";
  var count = 0;
  for (i = 0; i < checkString.length; i++) {
    ch = checkString.substring(i, i + 1);
    if ((ch >= "0" && ch <= "9") || ch == "-") {
      newString += ch;
    }
  }

  if (checkString != newString) {
    alert(
      "入力された値は不正な文字列です。\n電話(FAX)番号を入力してください。\n入力された文字列は削除されます。"
    );
    // 文字列を返す
    return newString;
  }
  return checkString;
}
//ボタンを押し不可にする
//志願者SEQをonChangeした時、ボタンを非活性化する
function btn_disabled() {
  document.forms[0].btn_update.disabled = true;
  document.forms[0].btn_del.disabled = true;

  document.forms[0].btn_add2.disabled = true;
  document.forms[0].btn_update2.disabled = true;
  document.forms[0].btn_del2.disabled = true;
}
//フォームの値が変更されたか判断する
function change_flg() {
  vflg = true;
}
//エンターキーをTabに変換
function changeEnterToTab(obj) {
  if (window.event.keyCode == "13") {
    //移動可能なオブジェクト
    var textFieldArray = document.forms[0].setTextField.value.split(",");

    for (var i = 0; i < textFieldArray.length; i++) {
      if (textFieldArray[i] == obj.name) {
        targetObject = eval(
          'document.forms[0]["' + textFieldArray[i + 1] + '"]'
        );
        targetObject.focus();
        return;
      }
    }
  }
  return;
}

//--------下画面--------
//サブミット
function btn_submit2(cmd) {
  if (cmd == "changeTest") {
    document.forms[0].RECEPTNO.value = "";
    return false;
  }
  if (cmd == "reset" && !confirm("{rval MSG106}")) {
    return true;
  }
  if (cmd == "delete2") {
    if (!confirm("{rval MSG103}")) return false;
  }
  if (cmd == "add2" || cmd == "update2" || cmd == "delete2") {
    if (document.forms[0].RECEPTNO.value == "") {
      alert("{rval MSG301}\n( 受験番号 )");
      return true;
    }
  }

  document.forms[0].cmd.value = cmd;
  document.forms[0].submit();
  return false;
}
//左リストから選択
function link_select(setKey, setParam) {
  var setKeys = setKey.split("_");
  document.forms[0].CHECK_KEY.value = setKey;
  document.forms[0].TESTDIV.value = setKeys[0];
  document.forms[0].RECEPTNO.value = setKeys[1];

  var setParams = setParam.split("_");
  document.forms[0].DESIREDIV.value = setParams[0];
  document.forms[0].SHDIV.value = setParams[1];
  document.forms[0].SUBCLASS_TYPE.value = setParams[2];
}
//受験番号をonChangeした時、ボタンを非活性化する
function btn_disabled2() {
  document.forms[0].btn_update2.disabled = true;
  document.forms[0].btn_del2.disabled = true;
}
//親画面へ（入学者の一覧画面）
function closeFunc() {
  if (!confirm("{rval MSG108}")) {
    return;
  }
  top.opener.document.forms[0].SEND_EXAMNO.value =
    document.forms[0].EXAMNO.value;
  top.opener.document.forms[0].submit();
  top.window.close();
}
