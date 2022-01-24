<?php

require_once('for_php7.php');

class knjz177_gradeModel extends Model {

    var $cmd;
    var $schregno = "";
    var $name = "";
    var $field = array();
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    function knjz177_gradeModel()
    {
    }

    function init() {
        if(VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
            $this->year     = VARS::get("SEND_YEAR");
            unset($this->grade);
            unset($this->school_kind);
            $this->field = array();
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;
        $this->programid = $this->getPrgId ? $this->getPrgId : PROGRAMID;
        
        //更新の値を取得
        for ($i = $this->firstcount; $i < $this->lastcount; $i++) {
            $this->field["APPOINTEDDAY".$i] =  VARS::post("APPOINTEDDAY".$i);
            $this->field["SEM_MONTH".$i]    =  VARS::post("SEM_MONTH".$i);
        }
        
        //学年パラメータセット用

        $this->cmd = VARS::request("cmd");
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //校種
        if (VARS::post("SCHOOL_KIND") != "") {
            $this->school_kind = VARS::post("SCHOOL_KIND");
        }
        //学年パラメータセット用
        if (VARS::request("GRADE") != "") {
            $this->grade = VARS::request("GRADE");
        }
        //履歴用GETパラメータ
        if (VARS::get("UPD_FLG") != "") {
            $this->getUpdFlg = VARS::get("UPD_FLG");
        }
    }

    //入力項目チェック
    function validateInput($db)
    {
    
        if ($this->grade == "") {
            $this->setWarning("MSG301", "(学年)");
            return false;
        }
        for ($i = $this->firstcount; $i < $this->lastcount; $i++) {
            list($semester, $month) = explode("-", $this->field["SEM_MONTH".$i]);
            //締め日文字数チェック
            if (strlen($this->field["APPOINTEDDAY".$i]) > 2) {
                $this->setWarning("MSG915","締め日 2バイトまでです");
                return false;
            }
            //数値であるかチェック
            if ($this->field["APPOINTEDDAY".$i] != strval(intval($this->field["APPOINTEDDAY".$i])) && $this->field["APPOINTEDDAY".$i] != '') {
                $this->setWarning("MSG901", "半角数字を入力してください。");
                return false;
            }
            //そのつきの最終日より大きいかチェック
            $setYear = $month < 4 ? $this->year + 1 : $this->year;
            $lastday = date("t", mktime( 0, 0, 0, $month, 1, $setYear));
            if ($this->field["APPOINTEDDAY".$i] > $lastday) {
                $this->setWarning("MSG901", "{$month}月の最終日は{$lastday}です。");
                return false;
            }
            //学期の境目をまたいでないかチェック
            $query = knjz177_gradeQuery::checker($this->year, $semester);
            $row = $db->getRow($query,DB_FETCHMODE_ASSOC);
            if ($month == $row['SMONTH'] && $this->field["APPOINTEDDAY".$i] < $row['SDAY'] && $this->field["APPOINTEDDAY".$i] != '') {
                $this->setWarning("MSG901", "日付が学期をまたいでいます。".$row['SMONTH']."月の開始日は{$row['SDAY']}日です");
                return false;
            } else if ($month == $row['EMONTH'] && $this->field["APPOINTEDDAY".$i] > $row['EDAY'] && $this->field["APPOINTEDDAY".$i] != '') {
                $this->setWarning("MSG901", "日付が学期をまたいでいます。".$row['EMONTH']."月の終了日は{$row['EDAY']}日です");
                return false;
            }
        }
        return true;
    }

    //更新
    function &getUpdateModel() {
        $db = Query::dbCheckOut();
        //入力チェック
        if ($this->validateInput($db)) {
            $db->autoCommit(false);
            $query = knjz177_gradeQuery::getDeleteQuery($this);
            $db->query($query);
            for ($i = $this->firstcount; $i < $this->lastcount; $i++) {
                list($semester, $month) = explode("-", $this->field["SEM_MONTH".$i]);
                if ($semester != "" && $month != "" && $this->field["APPOINTEDDAY".$i] != "") {
                    $query = knjz177_gradeQuery::getInsertQuery($this, $semester, $month, $this->field["APPOINTEDDAY".$i]);
                    $db->query($query);
                }
            }
            $db->commit(); // トランザクションをコミットする。
            $this->setMessage("MSG201");
        }
        Query::dbCheckIn($db);
    }
}
?>