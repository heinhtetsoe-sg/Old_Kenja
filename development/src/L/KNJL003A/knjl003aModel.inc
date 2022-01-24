<?php

require_once('for_php7.php');

//定数定義
class knjl003aModel extends Model
{
    public $field = array();
    public $cmd;
    public $applicantdiv;      //primary
    public $honordiv;          //primary
    public $year;              //入試年度
    public $NextYear;
    public $honorTypeLis;      //特待区分

    public function init()
    {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->applicantdiv);
            unset($this->honordiv);
        }

        $temp = VARS::request("year");
        if (isset($temp)) {
            $this->year = VARS::request("year");
        }
        $temp = VARS::request("APPLICANTDIV");
        if (isset($temp)) {
            $this->applicantdiv = VARS::request("APPLICANTDIV");
        }
        $temp = VARS::get("HONORDIV");
        if (isset($temp)) {
            $this->honordiv = VARS::get("HONORDIV");
        }

        $this->field = array("ENTEXAMYEAR"      =>  $this->year,                    //入試年度
                             "APPLICANTDIV"     =>  $this->applicantdiv,            //受験校種
                             "HONORDIV"         =>  VARS::post("HONORDIV"),         //特待CD
                             "HONORDIV_NAME"    =>  VARS::post("HONORDIV_NAME"),    //特待名称
                             "HONORDIV_ABBV"    =>  VARS::post("HONORDIV_ABBV"),    //特待略称
                             "CLUB_FLG"         =>  VARS::post("CLUB_FLG"),         //クラブ
                             "NOTICE_CLASS"     =>  VARS::post("NOTICE_CLASS"),     //通知書用区分1
                             "NOTICE_KIND"      =>  VARS::post("NOTICE_KIND"),      //通知書用区分2
                             "ENROLL_FEES"      =>  VARS::post("ENROLL_FEES"),      //入学金
                             "ENROLL_FEES2"     =>  VARS::post("ENROLL_FEES2"),     //入学金(高校)
                             "SCHOOL_FEES"      =>  VARS::post("SCHOOL_FEES"),      //授業料
                             "SCHOLARSHIP1"     =>  VARS::post("SCHOLARSHIP1"),     //奨学金（月額）
                             "SCHOLARSHIP2"     =>  VARS::post("SCHOLARSHIP2"),     //奨学金（年間）
                             "HONOR_TYPE"       =>  VARS::post("HONOR_TYPE"),       //特待区分
                             "PRIORITY"         =>  VARS::post("PRIORITY")          //優先順位
                             );

        //特待区分リスト
        $this->honorTypeList[1] = array("VALUE" => "1", "LABEL" => "事前特待");
        $this->honorTypeList[2] = array("VALUE" => "2", "LABEL" => "特待申請");
        $this->honorTypeList[3] = array("VALUE" => "3", "LABEL" => "資格活用");
        $this->honorTypeList[4] = array("VALUE" => "4", "LABEL" => "事前専願");

        $this->cmd = VARS::request("cmd");
    }

    //入力項目チェック
    public function validateInput($kind)
    {
        //空文字チェック
        if (strlen($this->field["APPLICANTDIV"]) == 0) {
            $this->setWarning("MSG301", "( 受験校種 )");
            return false;
        }
        if (strlen($this->field["HONORDIV"]) == 0) {
            $this->setWarning("MSG301", "( 特待CD )");
            return false;
        }
        if (strlen($this->field["HONORDIV_NAME"]) == 0) {
            $this->setWarning("MSG301", "( 特待名称 )");
            return false;
        }
        if (strlen($this->field["HONORDIV_ABBV"]) == 0) {
            $this->setWarning("MSG301", "( 特待略称 )");
            return false;
        }

        //キー値が変更されているかをチェック
        if (
            ($this->field["HONORDIV"] != $this->honordiv) &&
            ($kind=="update" || $kind=="delete")
        ) {
            $this->setWarning("MSG308");
            return false;
        }

        //１レコード取得
        $query = knjl003aQuery::getRow($this->field["ENTEXAMYEAR"], $this->field["APPLICANTDIV"], $this->field["HONORDIV"]);
        $db = Query::dbCheckOut();
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG918");
            return false;
        }
        //存在チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }

        if (($this->cmd == "update" || $this->cmd == "add")) {
            //文字数チェック
            if (strlen($this->field["HONORDIV_NAME"]) > 60) {
                $this->setWarning("MSG915", "( 特待名称は60バイトまでです。)");
                return false;
            }
            if (strlen($this->field["HONORDIV_ABBV"]) > 30) {
                $this->setWarning("MSG915", "( 特待略称は30バイトまでです。)");
                return false;
            }
    
            if ($this->field["APPLICANTDIV"] == '1') {
                $size = 6;
                $message = "6バイト";
            } else {
                $size = 30;
                $message = "全角10文字";
            }
            if (strlen($this->field["ENROLL_FEES"]) > $size) {
                $this->setWarning("MSG915", "( 入学金は{$message}までです。)");
                return false;
            }
    
            if (strlen($this->field["ENROLL_FEES2"]) > 6) {
                $this->setWarning("MSG915", "( 入学金は6バイトまでです。)");
                return false;
            }
    
            if (strlen($this->field["SCHOOL_FEES"]) > 6) {
                $this->setWarning("MSG915", "( 授業料（年額）は6バイトまでです。)");
                return false;
            }
            if (strlen($this->field["SCHOLARSHIP1"]) > 15) {
                $this->setWarning("MSG915", "( 奨学金（月額）は全角5文字までです。)");
                return false;
            }
            if (strlen($this->field["SCHOLARSHIP2"]) > 15) {
                $this->setWarning("MSG915", "( 奨学金（年間）は全角5文字までです。)");
                return false;
            }

            //優先順位のゼロ埋め
            if (strlen($this->field["PRIORITY"]) != 0) {
                $this->field["PRIORITY"] = sprintf("%02d", $this->field["PRIORITY"]);
            }
            //優先順位の一意チェック(重複不可)
            $row = "";
            $row = knjl003aQuery::chkUniqPriority($this->field);
            if (is_array($row)) {
                $this->setWarning("MSG302", "( 優先順位は重複しない数字を設定してください。)");
                return false;
            }
            //優先順位
            if (strlen($this->field["PRIORITY"]) > 2) {
                $this->setWarning("MSG915", "( 優先順位は2桁までです。)");
                return false;
            }
            //優先順位
            if (strlen($this->field["PRIORITY"]) > 0 && !preg_match('/^[0-9]+$/', $this->field["PRIORITY"])) {
                $this->setWarning("MSG915", "( 優先順位は数字のみ入力可能です。)");
                return false;
            }
        }

        return true;
    }

    //編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")) {
            knjl003aQuery::getUpdateQuery($this->field);
            $this->setMessage("MSG201");
        }
        return;
    }

    //追加用のモデルを返します
    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")) {
            knjl003aQuery::getInsertQuery($this->field);
            $this->setMessage("MSG201");
        }
        $this->honordiv = $this->field["HONORDIV"];
        return;
    }

    //削除用のモデルを返します
    public function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjl003aQuery::getDeleteQuery($this->field);
            $this->setMessage("MSG201");
            $this->field = "";
        }
        return;
    }

    //次年度のデータを作成
    public function &getCopyYearModel()
    {
        $this->nextYear = $this->year + 1;

        //今年度のデータが存在するか確認
        $t_year = knjl003aQuery::checkData($this->year);
        if (!isset($t_year)) {
            $this->setWarning('MSG203', "今年度のデータが存在しません。");
            return false;
        }
        //前年度のデータが存在するか確認
        $pre_year = knjl003aQuery::checkData($this->nextYear);
        if (isset($pre_year)) {
            $this->setWarning('MSG203', "既に次年度のデータが作成されています。");
            return false;
        }

        knjl003aQuery::getCopyQuery($this->year, $this->nextYear);
        $this->setMessage("MSG201");
        return;
    }

    //変数をクリアします
    public function clean()
    {
    }
}
