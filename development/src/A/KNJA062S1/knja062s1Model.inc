<?php

require_once('for_php7.php');

class knja062s1Model extends Model {
    var $fields = array();
    var $getParam = array();
    var $cmd;
    var $control;

    function knja062s1Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        if (VARS::get("YEAR")) {
            $this->getParam = array("YEAR"            =>  VARS::get("YEAR"),        //年度
                                    "SEMESTER"        =>  VARS::get("SEMESTER"),    //学期
                                    "GHR_CD"          =>  VARS::get("GHR_CD"),      //グループクラスコード
                                    "TR_DIV"          =>  VARS::get("TR_DIV"),      //担任区分
                                    "FROM_DATE"       =>  VARS::get("FROM_DATE")    //開始日付
                                    );
        }

        if ($this->cmd == "updlist") {
            $this->cmd = "list";
        }

        $this->fields = array("YEAR"            =>  $this->getParam["YEAR"],        //年度
                              "SEMESTER"        =>  $this->getParam["SEMESTER"],    //学期
                              "GHR_CD"          =>  $this->getParam["GHR_CD"],      //グループクラスコード
                              "TR_DIV"          =>  VARS::post("TR_DIV"),           //担任区分
                              "FROM_DATE"       =>  VARS::post("FROM_DATE"),        //開始日付
                              "TO_DATE"         =>  VARS::post("TO_DATE"),          //終了日付
                              "STAFFCD"         =>  VARS::post("STAFFCD"),          //担任
                              "UPDATED"         =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");
    }

    //入力項目チェック
    function validateInput()
    {

        if ($this->cmd=="update" || $this->cmd=="add") {

            //空文字チェック
            $strnotchk = array("YEAR"       => "( 年度 )",
                               "SEMESTER"   => "( 学期 )",
                               "GHR_CD"     => "( グループクラスコード )",
                               "TR_DIV"     => "( 担任区分 )",
                               "FROM_DATE"  => "( 開始日付 )",
                               "TO_DATE"    => "( 終了日付 )",
                               "STAFFCD"    => "( 担任 )"
                        );

            foreach ($strnotchk as $key => $val) {
                if (strlen($this->fields[$key]) <= 0) {
                    $this->setWarning("MSG301",$val);
                    return false;
                }
            }

            //キーの更新チェック
            $checkDate = preg_split("/\//", $this->fields["TO_DATE"]);
            $checkYear = $checkDate[1] < 4 ? $checkDate[0] - 1 : $checkDate[0];
            if ($checkYear != $this->fields["YEAR"]) {
                $this->setWarning("指定日付の年度が不正です。");
                return false;
            }
            $checkDate = preg_split("/\//", $this->fields["FROM_DATE"]);
            $checkYear = $checkDate[1] < 4 ? $checkDate[0] - 1 : $checkDate[0];
            if ($checkYear != $this->fields["YEAR"]) {
                $this->setWarning("指定日付の年度が不正です。");
                return false;
            }

            if (str_replace("/", "-", $this->fields["FROM_DATE"]) > str_replace("/", "-", $this->fields["TO_DATE"])) {
                $this->setWarning('開始日付より終了日付が小さいです。');
                return false;
            }

            $db = Query::dbCheckOut();
            $query = knja062s1Query::getData($this, "CHECK");
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $query = knja062s1Query::getSemesterMst($this);
            $semeDate = $db->getRow($query, DB_FETCHMODE_ASSOC);
            Query::dbCheckIn($db);

            //学期マスタチェック
            if ($semeDate["EDATE"] < str_replace("/", "-", $this->fields["FROM_DATE"])) {
                $this->setWarning('今学期の有効期間ではありません。');
                return false;
            }
            if (str_replace("/", "-", $this->fields["TO_DATE"]) < $semeDate["SDATE"]) {
                $this->setWarning('前任が今学期の有効期間ではありません。');
                return false;
            }

            //重複チェック
            if ($this->cmd == "add" && is_array($row)){
                $this->setWarning('MSG918');
                return false;
            }

        }

        if ($this->cmd=="update" || $this->cmd=="delete") {

            //キーの更新チェック
            if ($this->getParam["TR_DIV"] != $this->fields["TR_DIV"] ||
                $this->getParam["FROM_DATE"] != str_replace("/", "-", $this->fields["FROM_DATE"])
            ) {
                $this->setWarning("MSG308");
                return false;
            }

            //１レコード取得
            $db = Query::dbCheckOut();
            $query = knja062s1Query::getData($this, "CHECK");
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            Query::dbCheckIn($db);

            //データ存在チェック
            if (!is_array($row)) {
                $this->setWarning("MSG303");
                return false;
            }

            //更新日付チェック1
            if ($row["UPDATED"] == ""){
                $this->setWarning('MSG303');
                return false;
            }

        }

        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
            $this->updStaffClass('update');
            $this->updRegdGhrHdat();
            $this->getParam["TR_DIV"] = $this->fields["TR_DIV"];
            $this->getParam["FROM_DATE"] = str_replace("/", "-", $this->fields["FROM_DATE"]);
            $this->setMessage("MSG201");
        }
    }

    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->insert();
            $this->updStaffClass('insert');
            $this->updRegdGhrHdat();
            $this->getParam["TR_DIV"] = $this->fields["TR_DIV"];
            $this->getParam["FROM_DATE"] = str_replace("/", "-", $this->fields["FROM_DATE"]);
            $this->setMessage("MSG201");
        }
        return;
    }

    //DELETE
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $ret_val = knja062s1Query::getDeleteQuery($this);
            $this->updRegdGhrHdat();
            $this->setMessage("MSG201");
        }

    }

    //INSERT
    function &insert()
    {
        $result = knja062s1Query::getInsertQuery($this);
        return $result;
    }

    //UPDATE
    function &update()
    {
        $result = knja062s1Query::getUpdateQuery($this);
        return $result;
    }

    //STAFF_CLASS_HIST_GHR_DAT.TO_DATE更新
    function &updStaffClass($cmd)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $befTrDiv = "";
        $setDate = "";
        $result = $db->query(knja062s1Query::getList($this, "UPD"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($befTrDiv != $row["TR_DIV"]) {
                $setDate = ((int)$this->getParam["YEAR"] + 1)."-03-31";
            }

            if($cmd == 'update' && $row["TO_DATE"] <= $setDate) {
            } else if($cmd == 'insert' && $row["TO_DATE"] != $setDate && $row["TO_DATE"] <= $setDate){
            } else {
                $query = knja062s1Query::getUpdateToDate($row, $setDate);
                $db->query($query);
            }

            $setDate = date("Y-m-d", strtotime("-1 day"   ,strtotime($row["FROM_DATE"])));
            $befTrDiv = $row["TR_DIV"];
        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);
    }

    //SCHREG_REGD_GHR_HDAT更新
    function &updRegdGhrHdat()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knja062s1Query::getRegdGhrHdatUpdAll($this);
        $db->query($query);

        $result = $db->query(knja062s1Query::getList($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $query = knja062s1Query::getRegdGhrHdatUpd($row);
            $db->query($query);
        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
