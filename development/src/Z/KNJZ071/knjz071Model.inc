<?php

require_once('for_php7.php');

//定数定義

class knjz071Model extends Model {
    var $field      = array();
    var $cmd;
    var $subclasscd="";        //primary
    var $school_kind;
    var $curriculum_cd;

    //メンバ変数の初期化
    function knjz071Model()
    {
    }

    function init()
    {
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $mid ="" ;
        //コードのゼロ埋め
        if (VARS::post("SUBCLASSCD") != "") {
             $mid = sprintf("%04d",VARS::post("SUBCLASSCD"));
        }

        list($setClassCd, $setSchoolCd) = preg_split("/-/", VARS::post("CLASSCD"));
        $setClassCd = VARS::post("CLASSCD")."-".$setClassCd;
        $this->field = array("CLASSCD_SET"        =>  VARS::post("CLASSCD"),
                            "SUBCLASSCD"          =>  $setClassCd.$mid,                       //科目コード
                            "SCHOOL_KIND"         =>  $setSchoolCd,                           //学校校種
                            "CURRICULUM_CD"       =>  VARS::post("CURRICULUM_CD"),            //教育課程コード
                            "SUBCLASSNAME"        =>  VARS::post("SUBCLASSNAME"),             //科目名
                            "SUBCLASSABBV"        =>  VARS::post("SUBCLASSABBV"),             //科目略称
                            "SUBCLASSNAME_ENG"    =>  VARS::post("SUBCLASSNAME_ENG"),         //科目名英字
                            "SUBCLASSABBV_ENG"    =>  VARS::post("SUBCLASSABBV_ENG"),         //科目略称英字
                            "SUBCLASSORDERNAME1"  =>  VARS::post("SUBCLASSORDERNAME1"),       //科目名その他１
                            "SUBCLASSORDERNAME2"  =>  VARS::post("SUBCLASSORDERNAME2"),       //科目名その他２
                            "SUBCLASSORDERNAME3"  =>  VARS::post("SUBCLASSORDERNAME3"),       //科目名その他３
                            "SHOWORDER"           =>  VARS::post("SHOWORDER"),                //表示順
                            "SHOWORDER2"          =>  VARS::post("SHOWORDER2"),               //調査書用表示順
                            "SHOWORDER3"          =>  VARS::post("SHOWORDER3"),               //通知表用表示順
                            "SUBCLASSCD2"         =>  VARS::post("SUBCLASSCD2"),              //調査書・指導要録用科目グループコード
                            "SUBCLASSCD3"         =>  VARS::post("SUBCLASSCD3"),              //通知表用科目グループコード
                            "ELECTDIV"            =>  VARS::post("ELECTDIV"),                 //選択
                            "VALUATION"           =>  VARS::post("VALUATION"),                //評定
                            "GET_CREDIT"          =>  VARS::post("GET_CREDIT"),               //習得科目単位
                            "UPDATED"             =>  VARS::post("UPDATED")                   //更新日付
                            );

        $this->cmd             = VARS::request("cmd");
        if ($this->cmd == '') {
            unset($this->subclasscd);
            unset($this->school_kind);
            unset($this->curriculum_cd);
            unset($this->leftField);
            unset($this->leftSField);
            unset($this->sendAuth);
        }
        if (VARS::get("mode") == "1"){
            unset($this->subclasscd);
            unset($this->school_kind);
            unset($this->curriculum_cd);
        }
        if (VARS::get("SUBCLASSCD")!=""){
            $this->subclasscd = VARS::get("SUBCLASSCD");
        }
        if (VARS::get("SCHOOL_KIND")!="") {
            $this->school_kind   = VARS::get("SCHOOL_KIND");
        }
        if (VARS::get("CURRICULUM_CD")!="") {
            $this->curriculum_cd = VARS::get("CURRICULUM_CD");
        }

        if (VARS::post("SCHKIND") != "") {
            $this->leftField["SCHKIND"]         = VARS::post("SCHKIND");
        }
        if (VARS::post("S_CLASSCD") != "") {
            $this->leftField["S_CLASSCD"]       = VARS::post("S_CLASSCD");
        }
        if (VARS::post("S_CURRICULUM_CD") != "") {
            $this->leftField["S_CURRICULUM_CD"] = VARS::post("S_CURRICULUM_CD");
        }
        //呼出された場合
        if (VARS::get("SEND_PRGID")) {
            $this->leftSField = array("SCHKIND"          =>  VARS::get("SEND_SCHKIND"),
                                      "S_CLASSCD"        =>  VARS::get("SEND_CLASSCD"),
                                      "S_CURRICULUM_CD"  =>  VARS::get("SEND_CURRICULUM_CD"),
                                      "SEND_PRGID"       =>  VARS::get("SEND_PRGID")
                                     );
            $this->sendAuth = VARS::get("SEND_AUTH");
        }

        $this->auth = $this->sendAuth ? $this->sendAuth : AUTHORITY;
    }

    //入力項目チェック
    function validateInput($kind)
    {
        //１レコード取得
        $row = knjz071Query::getRow($this, "", "", $this->field["SUBCLASSCD"]);
        if ($kind=="update" || $kind=="delete") {                 //リストより選択していない
            if ($this->subclasscd==""){
                $this->setWarning("MSG308","キー値は変更できません");
                return false;
            }
            //キー値が変更されていますかをチェック
            if (substr($this->field["SUBCLASSCD"], 5, 6) != $this->subclasscd){
                $this->setWarning("MSG308","キー値は変更できません");
                return false;
            }
            //更新日付チェック1
            if ($this->cmd == "update" && $row["UPDATED"] == ""){
                $this->setWarning("MSG303");
                return false;
            }
            //更新日付チェック2
            if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]){
                $this->setWarning("MSG307");
                return false;
            }
        }
        if ($kind=="update" || $kind=="insert") {
            if (trim(substr($this->field["SUBCLASSCD"], 7, 6)) < 100) {
                $this->setWarning("MSG901","科目コードの下4桁は「0100」以上で登録おねがいします。");
                return false;
            }
            //重複チェック
            if ($this->cmd == "add" && is_array($row)){
                $this->setWarning("MSG918","(科目コード)");
                return false;
            }
            if (strlen($this->field["SUBCLASSNAME"]) >60 ) {
                $this->setWarning("MSG915","(科目名は60バイトまでです)");
                return false;
            } elseif (strlen($this->field["SUBCLASSNAME"])==0){
                $this->setWarning("MSG301","(科目名称)");
                return false;
            } elseif (strlen($this->field["SUBCLASSABBV"]) >9){
                $this->setWarning("MSG915","(科目略称は9バイトまでです)");
                return false;
            } elseif (strlen($this->field["SUBCLASSNAME_ENG"]) >50){ 
                $this->setWarning("MSG915","(科目名称英字は50バイトまでです)");
                return false; 
            } elseif (strlen($this->field["SUBCLASSABBV_ENG"]) >20){ 
                $this->setWarning("MSG915","(科目略称英字は20バイトまでです)");
                return false;
            } elseif (strlen($this->field["SUBCLASSORDERNAME1"]) >60){ 
                $this->setWarning("MSG915","(調査書用科目名は60バイトまでです)");
                return false;
            } elseif (strlen($this->field["SUBCLASSORDERNAME2"]) >60){ 
                $this->setWarning("MSG915","(通知票用科目名は60バイトまでです)");
                return false;
            } elseif (strlen($this->field["SUBCLASSORDERNAME3"]) >60){ 
                $this->setWarning("MSG915","(科目名その他３は60バイトまでです)");
                return false;
            } elseif (strlen($this->field["SHOWORDER"]) >2){ 
                $this->setWarning("MSG915","(表示順)");
                return false;
            } elseif (strlen($this->field["SHOWORDER2"]) >2){ 
                $this->setWarning("MSG915","(調査書用表示順)");
                return false;
            } elseif (strlen($this->field["SHOWORDER3"]) >2){ 
                $this->setWarning("MSG915","(通知表用表示順)");
                return false;
            } elseif (strlen($this->field["SUBCLASSCD2"]) >6){ 
                $this->setWarning("MSG915","(調査書・指導要録用科目グループコード)");
                return false;
            } elseif (strlen($this->field["SUBCLASSCD3"]) >6){ 
                $this->setWarning("MSG915","(通知表用科目グループコード)");
                return false;
            } elseif (strlen($this->field["VALUATION"]) >2){ 
                $this->setWarning("MSG915","(評定)");
                return false;
            } elseif (strlen($this->field["GET_CREDIT"]) >2){ 
                $this->setWarning("MSG915","(修得単位数)");
                return false;
            }
            //コードのゼロ埋め
            if ($this->field["SUBCLASSCD2"] !="" && strlen($this->field["SUBCLASSCD2"]) < 6) {
                $this->field["SUBCLASSCD2"] = sprintf("%06d",$this->field["SUBCLASSCD2"]);
            }
            if ($this->field["SUBCLASSCD3"] !="" && strlen($this->field["SUBCLASSCD3"]) < 6) {
                $this->field["SUBCLASSCD3"] = sprintf("%06d",$this->field["SUBCLASSCD3"]);
            }

            //存在チェック
            $row2 = knjz071Query::getRow_check($this, $this->field["SUBCLASSCD"], $this->field["SUBCLASSCD2"]);
            if ($this->field["SUBCLASSCD2"] !="" && !is_array($row2)) {
                $this->setWarning("MSG303","(調査書・指導要録用科目グループコード)");
                return false;
            }
            $row3 = knjz071Query::getRow_check($this, $this->field["SUBCLASSCD"], $this->field["SUBCLASSCD3"]);
            if ($this->field["SUBCLASSCD3"] !="" && !is_array($row3)) {
                $this->setWarning("MSG303","(通知表用科目グループコード)");
                return false;
            }
        }
        return true;
    }
    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")){
            $this->update();
        }
        $this->leftSField["SCHKIND"]         = $this->field["SCHOOL_KIND"];
        $this->leftSField["S_CLASSCD"]       = $this->field["CLASSCD_SET"];
        $this->leftSField["S_CURRICULUM_CD"] = $this->field["CURRICULUM_CD"];
    }
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")){
            $this->insert();
            $this->subclasscd = substr($this->field["SUBCLASSCD"], 5, 6);
            $this->school_kind = substr($this->field["SUBCLASSCD"], 3, 1);
            $this->curriculum_cd = $this->field["CURRICULUM_CD"];
            $this->leftSField["SCHKIND"]         = $this->field["SCHOOL_KIND"];
            $this->leftSField["S_CLASSCD"]       = $this->field["CLASSCD_SET"];
            $this->leftSField["S_CURRICULUM_CD"] = $this->field["CURRICULUM_CD"];
        }
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")){
            $this->delete();
        }
        return;
    } 
    function &delete()
    {
        if (!isset($this->subclasscd)) {
            $this->setWarning("MSG303");
            return;
        }
        $result = knjz071Query::getDeleteQuery($this->field, $this);
        if ( !isset($this->warning)){
            $this->setMessage("MSG201");
        }
        $this->subclasscd ="";
        $this->curriculum_cd = "";
        
        return $result;
    }
    function &insert()
    {
        $result = knjz071Query::getInsertQuery($this->field, $this);
        if ( !isset($this->warning)){
            $this->setMessage("MSG201");
        }
        return $result;
    }

    //アップデート時の内部メソッド
    function &update()
    {
        $result = knjz071Query::getUpdateQuery($this->field, $this);
        if ( !isset($this->warning)){
            $this->setMessage("MSG201");
        }
        return $result;
    }
    //変数をクリアします
    function clean()
    {
    }
}
?>
