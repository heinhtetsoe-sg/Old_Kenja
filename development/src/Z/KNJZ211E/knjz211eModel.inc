<?php

require_once('for_php7.php');

class knjz211eModel extends Model {
    var $field = array();
    var $data  = array();
    var $cmd;
    var $sendAuth;  //親画面の権限
    var $auth;      //権限
    
    function knjz211eModel() {}

    function init() {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            unset($this->sendAuth);
            unset($this->auth);
            common::GetControlMaster_Fnc($this->control);
        }
        
        //子画面から戻ったとき
        if (VARS::get("SEND_PRGRID")) {
            $this->getPrgId = VARS::get("SEND_PRGRID");
            $this->sendAuth = VARS::get("SEND_AUTH");
            
            $this->field["GRADE"]           = VARS::get("SEND_GRADE");
            $this->field["CLASSCD"]         = VARS::get("SEND_CLASSCD");
            $this->field["SUBCLASSCD"]      = VARS::get("SEND_SUBCLASSCD");
        }
        //コマンド変更時
        if (VARS::request("cmd") == "main") {
            $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                                 "GRADE"            =>  VARS::post("GRADE"),            //学年
                                 "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),       //科目
                                 "CLASSCD"          =>  VARS::post("CLASSCD"),          //教科
                                 "VIEWFLG1"         =>  VARS::post("VIEWFLG1"),         //1学期または前期の観点FRG
                                 "VIEWFLG2"         =>  VARS::post("VIEWFLG2"),         //2学期または後期の観点FRG
                                 "VIEWFLG3"         =>  VARS::post("VIEWFLG3"),         //3学期の観点FRG
                                 "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                                );
        }
        
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;
        $this->cmd     = VARS::request("cmd");
        
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

    }

    //入力項目チェック
    function validateInput() {

        return true;
    }

    function &getUpdateModel() {
        $db = Query::dbCheckOut();
        //初期化
        $this->fields = array();
        for ($i = 0; $i < get_count($this->data["VIEWCD"]); $i++) {
            $view_name1 = "VIEWFLG1"."-".$i;
            $view_name2 = "VIEWFLG2"."-".$i;
            $view_name3 = "VIEWFLG3"."-".$i;
            $this->fields["VIEWFLG1"][] = VARS::post($view_name1);
            $this->fields["VIEWFLG2"][] = VARS::post($view_name2);
            $this->fields["VIEWFLG3"][] = VARS::post($view_name3);
        }
        //データ更新前後の観点回数の確認用配列
        $viewcount_before = array();
        $viewcount_after = array();
        //データ更新前観点の設定回数取得
        for ($i = 0; $i < get_count($this->data["VIEWCD"]); $i++) {
            $viewcd = $this->data["VIEWCD"][$i];
            $viewcount_before[$viewcd] = $db->getOne(knjz211eQuery::count($this, $viewcd));
        }
        
        if ($this->validateInput()) {
            knjz211eQuery::delete($this, $db);
            knjz211eQuery::insJviewstatInputSeqDat($this, $db, "1");
            knjz211eQuery::insJviewstatInputSeqDat($this, $db, "2");
            //3学期がある場合のみ
            if ($this->semester_count == 3) {
                knjz211eQuery::insJviewstatInputSeqDat($this, $db, "3");
            }
            //データ更新後の観点の設定回数取得
            for ($i = 0; $i < get_count($this->data["VIEWCD"]); $i++) {
                $viewcd = $this->data["VIEWCD"][$i];
                $viewcount_after[$viewcd] = $db->getOne(knjz211eQuery::count($this, $viewcd));
            }
            //データ更新後で観点回数が異なる場合、対象のVIEWCDの満点を削除する
            $diff = 0;//観点回数変更カウント用の変数
            for ($i = 0; $i < get_count($this->data["VIEWCD"]); $i++) {
                $viewcd = $this->data["VIEWCD"][$i];
                if ($viewcount_before[$viewcd] != $viewcount_after[$viewcd]) {
                    knjz211eQuery::deleteManten($this, $viewcd, $db);
                    $diff++;
                }
            }
            if ($diff > 0) {
                $this->setMessage("MSG201","観点回数が変更されています。学年観点別評価設定にて段階値の範囲を見直して下さい。");
            } else {
                $this->setMessage("MSG201");
            }
        }
        Query::dbCheckIn($db);
        return true;
    }
}
//var_dump($_REQUEST);
?>
