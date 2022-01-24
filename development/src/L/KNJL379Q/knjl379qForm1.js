function btn_submit(cmd) {
    
    if(cmd == 'update'){      //更新のときのエラーチェック
        var judgeUse = document.forms[0].judgeUse.value;    //使用している判定のコード
        var judge = judgeUse.split(',');
        var cnt = judge.length;
        for(j=0;j<4;j++){
            for(c=0;c<cnt;c++){
                i = judge[c];
                
                if((document.forms[0]["FROM_"+j+i].value == "" && document.forms[0]["TO_"+j+i].value != "")
                    || (document.forms[0]["FROM_"+j+i].value != "" && document.forms[0]["TO_"+j+i].value == "")){
                        alert('点数上限・点数下限の片方のみの入力は出来ません。');
                        document.forms[0]["FROM_"+j+i].focus();
                        return false;
                    }
                if(document.forms[0]["FROM_"+j+i].value > document.forms[0]["TO_"+j+i].value){
                    alert('点数が前後しています。');
                    document.forms[0]["FROM_"+j+i].focus();
                    return false;
                }
                if(c != 0 && document.forms[0]["TO_"+j+i].value != ""){     //入力されてるときだけ
                    var a = judge[c-1];
                    var pass = document.forms[0]["FROM_"+j+a].value;
                    if(pass == ""){ //1つ上に入ってなかったら2つ上を確認
                        var b = judge[c-2];
                        pass = document.forms[0]["FROM_"+j+b].value
                    }
                    pass = pass -1;
                    if(pass != document.forms[0]["TO_"+j+i].value){
                        alert('入力した点数が正しくありません。');
                        document.forms[0]["FROM_"+j+a].focus();
                        return false;
                    }
                }
            }
        }
    }else if (cmd == 'copy'){
        if(document.forms[0].COPY_YEAR.value == ''){
            alert('コピーする年度を選択してください。');
            return false;
        }else{
            if(!confirm('現年度の作成済みデータはすべて削除されますが、コピーしてよろしいですか？')){
                return false;
            }
        }
    }

    
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//権限チェック
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}

//セキュリティーチェック
function OnSecurityError()
{
    alert('{rval MSG300}' + '\n高セキュリティー設定がされています。');
    closeWin();
}

