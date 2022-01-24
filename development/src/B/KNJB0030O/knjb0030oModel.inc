<?php

class knjb0030oModel extends Model {
    var $fields      = array();
    var $cmd;
    var $term;
    var $term2;
    var $control;
    var $check;
    var $checkGrddiv;
    var $group;
    var $chaircd;       //primary
    var $defFlg;

    function knjb0030oModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        if (VARS::request("term") != "")
            $this->term  = VARS::request("term");

        if (VARS::request("term2") != "")
            $this->term2 = VARS::request("term2");

        if ((VARS::request("PROGRAMID") != "") && (CTRL_SEMESTER != "1")) {
            $semester = CTRL_SEMESTER -1;
            $this->term2  = CTRL_YEAR. "-" .$semester;
        }

        if ((VARS::request("PROGRAMID") != "") && (CTRL_SEMESTER == "1")) {
            $year = CTRL_YEAR - 1;
            $semester = common::GetMasterData("select max(semester) as semester from semester_mst where year='".$year."' and semester<>'9'");
            $this->term2  = $year. "-" .$semester["SEMESTER"];
        }

        if ((VARS::request("PROGRAMID") != ""))
            $this->term  = CTRL_YEAR. "-" .CTRL_SEMESTER;

        if (VARS::request("group") != "")
            $this->group = VARS::request("group");

        if ((VARS::request("PROGRAMID") != ""))
            unset($this->group);

        $this->fields = array("CHAIRCD"         =>  VARS::post("CHAIRCD"),
                              "GROUPCD"         =>  VARS::post("GROUPCD"),
                              "SUBCLASSCD"      =>  VARS::post("SUBCLASSCD"),
                              "CHAIRNAME"       =>  VARS::post("CHAIRNAME"),
                              "CHAIRABBV"       =>  VARS::post("CHAIRABBV"),
                              "TAKESEMES"       =>  VARS::post("TAKESEMES"),
                              "LESSONCNT"       =>  VARS::post("LESSONCNT"),
                              "FRAMECNT"        =>  VARS::post("FRAMECNT"),
                              "COUNTFLG"        =>  VARS::post("COUNTFLG"),         //集計フラグ---05/01/19Add

                              "FACILITYABBV"    =>  VARS::post("FACILITYABBV"),
                              "FACCD"           =>  VARS::post("FACCD"),
                              "TEXTBOOKABBV"    =>  VARS::post("TEXTBOOKABBV"),
                              "TEXTBOOKCD"      =>  VARS::post("TEXTBOOKCD"),
                              "STAFFNAME_SHOW1" =>  VARS::post("STAFFNAME_SHOW1"),
                              "STAFFNAME_SHOW"  =>  VARS::post("STAFFNAME_SHOW"),
                              "STAFFCD"         =>  VARS::post("STAFFCD"),
                              "CHARGEDIV"       =>  VARS::post("CHARGEDIV"),
                              "STF_CHARGE"      =>  VARS::post("STF_CHARGE"),
                              "HR_NAMEABBV"     =>  VARS::post("HR_NAMEABBV"),
                              "GRADE_CLASS"     =>  VARS::post("GRADE_CLASS"),
                              "UPDATED"         =>  VARS::post("UPDATED"),

                              "sort_name"       =>  VARS::get("sort_name")
                             );

        $this->cmd   = VARS::request("cmd");

        if ($this->cmd == "") $this->defFlg = "on";

        $this->check = VARS::post("check");
        $this->checkGrddiv = VARS::post("checkGrddiv");

        if ($this->cmd == "") {
            unset($this->chaircd);
        }

        $temp = VARS::get("CHAIRCD");
        if (isset($temp))
            $this->chaircd  = VARS::get("CHAIRCD");

        if (VARS::get("init") == "1")
            unset($this->chaircd);

        if ($this->cmd == '') {
            unset($this->sort_name);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    //入力項目チェック
    function validateInput()
    {
        //リストよりデータを選択したかどうかをチェック---2004.04.22
        if ($this->cmd == "update" && !strlen($this->chaircd)) {
            $this->setWarning("MSG308");
            return false;
        }
        //キーの更新チェック
        if (trim($this->fields["CHAIRCD"]) == "") {
            $this->setWarning("MSG301", "(講座コード)");
            return false;
        }
        if ($this->fields["SUBCLASSCD"] == "") {
            $this->setWarning("MSG301", "(科目コード)");
            return false;
        }
        if (sprintf("%d", $this->fields["CHAIRCD"]) < 1) {
            $this->warning = "１以上を入力して下さい。(講座コード)";
            return false;
        }
        //講座コードゼロ埋め
        if (strlen($this->fields["CHAIRCD"]) < 7 ) {
            $this->fields["CHAIRCD"] = sprintf("%07d", $this->fields["CHAIRCD"]);
        }
        //文字数チェック
        if (strlen($this->fields["CHAIRCD"]) > 7 ) {
            $this->setWarning("MSG915", "(講座コード) 7バイトまでです。");
            return false;
        } elseif (strlen($this->fields["CHAIRNAME"]) > 30 ) {
            $this->setWarning("MSG915", "(講座名称) 30バイトまでです。");
            return false;
        } elseif (strlen($this->fields["CHAIRABBV"]) > 15 ) {
            $this->setWarning("MSG915", "(講座略称) 15バイトまでです。");
            return false;
        }
        //大小チェック（連続枠数＞週授業回数ならエラー）
        if ($this->fields["FRAMECNT"] > $this->fields["LESSONCNT"] ) {
            $this->warning = "連続授業が授業回数を超えています。";
            return false;
        }
        //リストよりデータを選択後、講座コードを変更した場合---2004.04.22
        if ($this->cmd == "update" && $this->chaircd != $this->fields["CHAIRCD"]) {
            $this->setWarning("MSG308");
            return false;
        }
        //重複チェック---2004.04.22
        $db = Query::dbCheckOut();
        $ret_val = $db->getOne(knjb0030oQuery::Count_Chair($this));
        if ($this->cmd == "insert" && $ret_val > 0) {
            //群コードチェック---2004.04.25
            $ret_val2 = $db->getOne(knjb0030oQuery::ChecktoGroupcd($this));
            if ($ret_val2 > 0) $group = " 教科に存在しています。";
            else               $group = " 群に存在しています。";
            $this->setWarning("MSG302", "(講座コード)".$group);
            return false;
        }
        Query::dbCheckIn($db);
        //存在チェック---2004.04.22
        if ($this->cmd == "update" && $ret_val == 0) {
            $this->setWarning("MSG303", "(講座コード)");
            return false;
        }
        return true;
    }

    //追加および更新
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
        }
    }
    //DELETE
    function &getDeleteModel()
    {
        //キーの更新チェック
        if (!strlen($this->chaircd)) {
            $this->setWarning("MSG308");
            return false;
        }
        if ($this->chaircd != $this->fields["CHAIRCD"]) {
            $this->setWarning("MSG308");
            return false;
        }

        $ret_val = knjb0030oQuery::getDeleteQuery($this);
        unset($this->chaircd);
        $this->message = "データを削除しました。";
    }

    //UPDATE（データがない場合、追加。ある場合、更新。）
    function &update()
    {
        $result = knjb0030oQuery::getUpdateQuery($this);
        if ($result == "Insert") {
            $this->chaircd = $this->fields["CHAIRCD"];
            if ($this->fields["GROUPCD"]=="0000") {
                $this->group = "1";
            } else {
                $this->group = "2";
            }
            $this->message = "データを新規作成しました。\\n生徒データは名簿入力画面にて行ってください。";
        } elseif($result == "Update") {
            $this->chaircd = $this->fields["CHAIRCD"];
            if ($this->fields["GROUPCD"]=="0000") {
                $this->group = "1";
            } else {
                $this->group = "2";
            }
            $this->setMessage("MSG201","生徒データは名簿入力画面にて行ってください。");
        }
        return;
    }

    //コピーボタン押し時の処理
    function &getInsertCopyModel()
    {
        //「コピーボタン」押し時に,コピー元の講座データが存在するかをチェック
        $count = knjb0030oQuery::Count($this->term2);
        if($count > "0"){
        } else {
            $this->warning="コピー元のデータが存在しません。別の参照学期を選択してから再度実行してください。";
            return false;
        }
        //対象年度の在籍ヘッダデータの存在チェック---2005.02.23Add
        $count = knjb0030oQuery::countSchregRegd($this->term,"schreg_regd_hdat");
        if($count > "0"){
        } else {
            $this->warning="対象年度に在籍ヘッダデータが存在しません。";
            return false;
        }
        //生徒もコピー
        if($this->check==1){
            //対象年度の在籍データの存在チェック---2005.02.23Add
            $count = knjb0030oQuery::countSchregRegd($this->term,"schreg_regd_dat");
            if($count > "0"){
            } else {
                $this->warning="対象年度に在籍データが存在しません。";
                return false;
            }
            //参照先の講座受講生徒データが妥当なデータか確かめる(DBエラー(重複)で落とさないため)
            $count = knjb0030oQuery::checkChairStdDat($this->term2);
            if($count > "0"){
                $this->warning="講座受講生徒データが不正です。コピーできませんでした。";
                return false;
            }
        }

        $Row = common::GetMasterData("select sdate,edate from semester_mst where year || '-' || semester = '".$this->term."'");
        knjb0030oQuery::getInsertCopyQuery($this,$this->term2,$this->term,$this->check,$Row["SDATE"],$Row["EDATE"],$this->checkGrddiv);
        //unset($this->check);
        //unset($this->checkGrddiv);
        $this->setMessage("MSG201");
        return;
    }
}
?>
