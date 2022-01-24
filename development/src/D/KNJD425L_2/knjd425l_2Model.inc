<?php

require_once('for_php7.php');

class knjd425l_2Model extends Model {

    var $cmd;
    var $schregno;
    var $name;
    var $grade;                    //左フレームから送られてくる年組
    var $selKindNo;                //別画面に引き継ぐKIND_NO
    var $control;
    var $exp_year;                 //左フレームから送られてくる年度
    var $exp_semester;             //左フレームから送られてくる学期
    var $sendPrgId;                //コールされたか
    var $sendAuth;                 //親画面の権限
    var $auth;                     //権限

    var $upddate;                  //別画面に引き継ぐ更新日
    var $subclassname;             //前年度「年間のまとめ」参照 に引き継ぐ科目名
    var $subclasscd;               //前年度「年間のまとめ」参照 に引き継ぐ科目コード
    var $remarkarry;               //入力項目
    var $subclasscdarry = array(); //入力項目の科目コード

    function knjd425l_2Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->sendPrgId);
            unset($this->sendAuth);
            unset($this->auth);

            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->selKindNo);
            unset($this->upddate);
            unset($this->field);
        }

        if (VARS::get("SEND_PRGID")) {
            $this->sendPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth  = VARS::get("SEND_AUTH");
        }
        //権限
        $this->auth = $this->sendPrgId ? $this->sendAuth : AUTHORITY;

        $this->cmd = VARS::request("cmd");

        //前画面から送られてくる
        //年度
        if (VARS::get("EXP_YEAR")) {
            $this->exp_year = VARS::get("EXP_YEAR");
        }
        //学期
        if (VARS::get("EXP_SEMESTER")) {
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }
        //学籍番号
        if (VARS::get("SCHREGNO")) {
            $this->schregno = VARS::get("SCHREGNO");
        }
        //氏名
        if (VARS::get("NAME")) {
            $this->name = VARS::get("NAME");
        }
        if (VARS::get("GRADE")) {
            $this->grade = VARS::get("GRADE");
        }
        if (VARS::get("KINDNO")) {
            $this->selKindNo = VARS::get("KINDNO");
        }
        if (VARS::get("UPDDATE")) {
            $this->upddate = VARS::get("UPDDATE");
            $this->recordDate = VARS::get("UPDDATE");
        }

        if (VARS::post("RECORD_DATE")) {
            $this->recordDate = VARS::post("RECORD_DATE");
        }
        if ($this->recordDate) {
            $this->recordDate = str_replace("/", "-", $this->recordDate);
        }

        //各textareaの最大文字数
        $this->remarkTextLimit = array(
               "1"  =>  array("moji" => 40, "gyou" => 10)
            ,  "2"  =>  array("moji" => 40, "gyou" => 10)
            ,  "3"  =>  array("moji" => 40, "gyou" => 10)
            ,  "4"  =>  array("moji" => 40, "gyou" => 10)
            ,  "5"  =>  array("moji" => 40, "gyou" => 10)
            ,  "6"  =>  array("moji" => 40, "gyou" => 10)
        );

        $db = Query::dbCheckOut();
        //項目タイトル取得
        $this->remarkTitle = array();
        $query = knjd425l_2Query::getHreportGuidanceKindNamedat($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $seq = intval($row["KIND_SEQ"]);

            $remarkData = array();
            $remarkData["KIND_SEQ"]     = $row["KIND_SEQ"];
            $remarkData["REMARK_TITLE"] = $row["KIND_REMARK"];
            $this->remarkTitle[$seq] = $remarkData;
        }
        $result->free();
        Query::dbCheckIn($db);

        $this->field = array();
        //テキストエリアの値取得
        foreach ($this->remarkTitle as $seq => $remarkTitle) {
            $this->field["REMARK_".$seq] = VARS::post("REMARK_".$seq);
        }

    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }

        //文字数チェック
        if ($this->cmd == "update") {
            foreach ($this->remarkTitle as $seq => $remarkTitle) {
                $moji = $this->remarkTextLimit[$seq]["moji"];
                $gyou = $this->remarkTextLimit[$seq]["gyou"];

                if ($this->validate_row_cnt($this->field["REMARK_{$seq}"], ($moji * 2)) > $gyou) {
                    $this->setWarning("MSG915","({$remarkTitle["REMARK_TITLE"]}は{$moji}文字X{$gyou}行までです。)");
                    return false;
                }

            }
        }
        return true;
    }

    //更新用のモデルを返します
    function &getUpdateModel() {
        if (!$this->validateInput()) {
            $this->setCmd("check");
            return;
        }

        knjd425l_2Query::getUpdateQuery($this);
        $this->setCmd("edit");
        $this->setMessage("MSG201");
    }

}
?>
