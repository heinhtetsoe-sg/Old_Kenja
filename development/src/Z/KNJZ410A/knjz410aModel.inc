<?php

require_once('for_php7.php');

//定数定義
class knjz410aModel extends Model {
    var $field      = array();
    var $cmd;
    var $school_cd;
    var $campus_addr_cd;
    //メンバ変数の初期化
    function knjz410aModel() {}

    function init() {
        $this->field = array("SCHOOL_CD"          =>  VARS::post("SCHOOL_CD"),
                             "CAMPUS_ADDR_CD"     =>  VARS::post("CAMPUS_ADDR_CD"),
                             "PREF_CD"            =>  VARS::post("PREF_CD"),
                             "ZIPCD"              =>  VARS::post("ZIPCD"),
                             "ADDR1"              =>  VARS::post("ADDR1"),
                             "ADDR2"              =>  VARS::post("ADDR2"),
                             "TELNO"              =>  VARS::post("TELNO"),
                             "SCHOOL_NAME"        =>  VARS::post("SCHOOL_NAME"),
                             "SCHOOL_GROUP"       =>  VARS::post("SCHOOL_GROUP"),
                             "SCHOOL_SORT"        =>  VARS::post("SCHOOL_SORT"),
                             "BUNYA"              =>  VARS::post("BUNYA"),
                             "AREA_NAME"          =>  VARS::post("AREA_NAME"),
                             "GREDES"             =>  VARS::post("GREDES"),
                             "SCHOOL_NAME_SHOW1"  =>  VARS::post("SCHOOL_NAME_SHOW1"),
                             "SCHOOL_NAME_SHOW2"  =>  VARS::post("SCHOOL_NAME_SHOW2"),
                             "BASE_SCORE"         =>  VARS::post("BASE_SCORE"),
                             "NECESSARY_SCORE"    =>  VARS::post("NECESSARY_SCORE"),
                             );

        $this->cmd             = VARS::request("cmd");
        if ($this->cmd == '') {
            $this->school_cd = "";
        }
        if (VARS::get("SCHOOL_CD")!="") {
            $this->school_cd    = VARS::get("SCHOOL_CD");
        }


        if (preg_match('/^add_addr/', $this->cmd)) { //住所コードは住所コード更新の画面でのみ使用する
            if (VARS::get("CAMPUS_ADDR_CD")!="") {
                $this->campus_addr_cd    = VARS::get("CAMPUS_ADDR_CD");
            }
        } else {
            unset($this->campus_addr_cd);
        }
    }
    //入力項目チェック
    function validateInput($kind) {
        if (preg_match('/^add_addr/', $this->cmd)) { //住所の更新の時
            if (trim($this->field["CAMPUS_ADDR_CD"]) == ""){
                $this->setWarning("MSG301","(住所コード)");
                return false;
            }
            if ($kind=="update" || $kind=="delete"){
                //リストより選択していない
                if ($this->campus_addr_cd==""){
                    $this->setWarning("MSG308");
                    return false;
                }
                //キー値が変更されているかをチェック
                if ($this->field["CAMPUS_ADDR_CD"] !=$this->campus_addr_cd){
                    $this->setWarning("MSG308","キー値は変更できません");
                    return false;
                }

                //コードのゼロ埋め
                $this->field["CAMPUS_ADDR_CD"] = sprintf("%02d",$this->field["CAMPUS_ADDR_CD"]);
                //１レコード取得
                $db = Query::dbCheckOut();
                $query = knjz410aQuery::getCollegeCampusAddrDat($this->school_cd, $this->field["CAMPUS_ADDR_CD"]);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                Query::dbCheckIn($db);

                //存在チェック
                if (!is_array($row)){
                    $this->setWarning("MSG303");
                    return false;
                }
            } else { //インサートの時
                //コードのゼロ埋め
                $this->field["CAMPUS_ADDR_CD"] = sprintf("%02d",$this->field["CAMPUS_ADDR_CD"]);
                //１レコード取得
                $db = Query::dbCheckOut();
                $query = knjz410aQuery::getCollegeCampusAddrDat($this->school_cd, $this->field["CAMPUS_ADDR_CD"]);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                Query::dbCheckIn($db);

                //存在チェック
                if (is_array($row)){
                    $this->setWarning("MSG302");
                    return false;
                }
            }
            if ($kind=="update" || $kind=="insert"){

                //コードのゼロ埋め
                $this->field["CAMPUS_ADDR_CD"] = sprintf("%02d",$this->field["CAMPUS_ADDR_CD"]);
                //１レコード取得
                $db = Query::dbCheckOut();
                $query = knjz410aQuery::getCollegeCampusAddrDat($this->school_cd, $this->field["CAMPUS_ADDR_CD"]);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                Query::dbCheckIn($db);

                //重複チェック
                if ($this->cmd == "add_addr_add" && is_array($row)){
                    $this->setWarning("MSG918","(住所コード)");
                    return false;
                }
                if (strlen($this->field["ZIPCD"]) > 8){
                    $this->setWarning("MSG915","(郵便番号は8バイトまでです。)");
                    return false;
                } elseif (strlen($this->field["ADDR1"]) > 90){
                    $this->setWarning("MSG915","(住所１は90バイトまでです。)");
                    return false;
                } elseif (strlen($this->field["ADDR2"]) > 90){
                    $this->setWarning("MSG915","(住所２は90バイトまでです。)");
                    return false;
                } elseif (strlen($this->field["TELNO"]) > 16){
                    $this->setWarning("MSG915","(電話番号は16バイトまでです。)");
                    return false;
                } elseif (strlen($this->field["BASE_SCORE"]) > 20){
                    $this->setWarning("MSG915","(基準点は20バイトまでです。)");
                    return false;
                } elseif (strlen($this->field["NECESSARY_SCORE"]) > 20){
                    $this->setWarning("MSG915","(必要点は20バイトまでです。)");
                    return false;
                }
            }
        } else {
            if (trim($this->field["SCHOOL_CD"]) == ""){
                $this->setWarning("MSG301","(学校コード)");
                return false;
            }

            if ($kind=="update" || $kind=="delete"){
                //リストより選択していない
                if ($this->school_cd==""){
                    $this->setWarning("MSG308");
                    return false;
                }
                //キー値が変更されているかをチェック
                if ($this->field["SCHOOL_CD"] !=$this->school_cd){
                    $this->setWarning("MSG308","キー値は変更できません");
                    return false;
                }

                //コードのゼロ埋め
                $this->field["SCHOOL_CD"] = sprintf("%08d",$this->field["SCHOOL_CD"]);
                //１レコード取得
                $row = knjz410aQuery::getCollegeMst_update($this->field["SCHOOL_CD"]);

                //存在チェック
                if (!is_array($row)){
                    $this->setWarning("MSG303");
                    return false;
                }
            }
            if ($kind=="update" || $kind=="insert"){

                //コードのゼロ埋め
                $this->field["SCHOOL_CD"] = sprintf("%08d",$this->field["SCHOOL_CD"]);
                //１レコード取得
                $row = knjz410aQuery::getCollegeMst_update($this->field["SCHOOL_CD"]);

                //重複チェック
                if ($this->cmd == "add" && is_array($row)){
                    $this->setWarning("MSG918","(学校コード)");
                    return false;
                }
                if (strlen($this->field["SCHOOL_NAME"]) > 120){
                    $this->setWarning("MSG915","(学校名称は120バイトまでです。)");
                    return false;
                } elseif (strlen($this->field["KANAME"]) > 120){
                    $this->setWarning("MSG915","(学科名は120バイトまでです。)");
                    return false;
                } elseif (strlen($this->field["AREA_NAME"]) > 30){
                    $this->setWarning("MSG915","(所在地は30バイトまでです。)");
                    return false;
                } elseif (strlen($this->field["GREDES"]) > 120){
                    $this->setWarning("MSG915","(評定基準は120バイトまでです。)");
                    return false;
                } elseif (strlen($this->field["SCHOOL_NAME_SHOW1"]) > 120){
                    $this->setWarning("MSG915","(表示用学校名1は120バイトまでです。)");
                    return false;
                } elseif (strlen($this->field["SCHOOL_NAME_SHOW2"]) > 120){
                    $this->setWarning("MSG915","(表示用学校名2は120バイトまでです。)");
                    return false;
                } elseif (strlen($this->field["BASE_SCORE"]) > 20){
                    $this->setWarning("MSG915","(基準点は20バイトまでです。)");
                    return false;
                } elseif (strlen($this->field["NECESSARY_SCORE"]) > 20){
                    $this->setWarning("MSG915","(必要点は20バイトまでです。)");
                    return false;
                }

            }
        }
        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput("update")){
            $this->update();
        }
    }
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput("insert")){
            $this->insert();
            $this->school_cd = $this->field["SCHOOL_CD"];
        }
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("delete")){
            $this->delete();
        }
        return;
    }
    function &delete() {
        if (preg_match('/^add_addr/', $this->cmd)) { //住所の更新の時
            if (!isset($this->campus_addr_cd)) {
                $this->setWarning("MSG303");
                return;
            }
            $result = knjz410aQuery::getDeleteCampusQuery($this);
            if ( !isset($model->warning)){
                $this->setMessage("MSG201");
                $this->field = array();
            }
        } else {
            if (!isset($this->school_cd)) {
                $this->setWarning("MSG303");
                return;
            }
            $result = knjz410aQuery::getDeleteQuery($this->field);
            if ( !isset($model->warning)){
                $this->setMessage("MSG201");
                $this->field = array();
            }
        }
    }

    function &insert() {
        if (preg_match('/^add_addr/', $this->cmd)) { //住所の更新の時
            $result = knjz410aQuery::getInsertCampusQuery($this);
            if ( !isset($model->warning)){
                $this->setMessage("MSG201");
                $this->campus_addr_cd = $this->field["CAMPUS_ADDR_CD"];
            }
            return $result;
        } else {
            $query  = knjz410aQuery::getCollegeCampusAddrDat($this->field["SCHOOL_CD"], $this->field["CAMPUS_ADDR_CD"]);
            $db = Query::dbCheckOut();
            $collegeCampusAddrDat = $db->getRow($query, DB_FETCHMODE_ASSOC);
            Query::dbCheckIn($db);
            $result = knjz410aQuery::getInsertQuery($this->field, $collegeCampusAddrDat);
            if ( !isset($model->warning)){
                $this->setMessage("MSG201");
            }
            return $result;
        }
    }

    //アップデート時の内部メソッド
    function &update() {
        if (preg_match('/^add_addr/', $this->cmd)) { //住所の更新の時
            $result = knjz410aQuery::getUpdateCampusQuery($this);
            if ( !isset($model->warning)){
                $this->setMessage("MSG201");
            }
            return $result;
        } else {
            $query  = knjz410aQuery::getCollegeCampusAddrDat($this->field["SCHOOL_CD"], $this->field["CAMPUS_ADDR_CD"]);
            $db = Query::dbCheckOut();
            $collegeCampusAddrDat = $db->getRow($query, DB_FETCHMODE_ASSOC);
            Query::dbCheckIn($db);
            $result = knjz410aQuery::getUpdateQuery($this->field, $collegeCampusAddrDat);
            if ( !isset($model->warning)){
                $this->setMessage("MSG201");
            }
            return $result;
        }
    }
    //変数をクリアします
    function clean() {}
}
?>
