<?php

require_once('for_php7.php');

class knjl211rModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $applicantdiv;      //入試制度
    var $field = array();           //フィールドデータ
    var $fixApplicantDiv;   //入試制度(ログイン校種により固定)

    function knjl211rModel()
    {
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
        }

        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR + 1;
        $this->semester     = CTRL_SEMESTER;
        $this->applicantdiv = VARS::post("APPLICANTDIV");
        $this->examno       = VARS::post("EXAMNO");

        $this->fixApplicantDiv = '1';
        if (SCHOOLKIND == 'J') {
                $this->fixApplicantDiv = '2';
        }

        //フィールド
        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),       //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),            //入試区分
                             "BEFORE_PAGE"      =>  VARS::post("BEFORE_PAGE"),        //事前頁
                             "BEFORE_SEQ"       =>  VARS::post("BEFORE_SEQ"),         //事前連番
                             "NAME"             =>  VARS::post("NAME"),               //氏名
                             "NAME_KANA"        =>  VARS::post("NAME_KANA"),          //氏名かな
                             "SEX"              =>  VARS::post("SEX"),                //性別
                             "EXAMCOURSECD"     =>  VARS::post("EXAMCOURSECD"),       //内諾
                             "FS_CD"            =>  VARS::post("FINSCHOOLCD"),        //出身学校コード
                             "NAISIN1"          =>  VARS::post("NAISIN1"),            //内申１
                             "NAISIN2"          =>  VARS::post("NAISIN2"),            //内申２
                             "NAISIN3"          =>  VARS::post("NAISIN3"),            //内申３
                             "ATTEND1"          =>  VARS::post("ATTEND1"),            //欠席１
                             "ATTEND2"          =>  VARS::post("ATTEND2"),            //欠席２
                             "ATTEND3"          =>  VARS::post("ATTEND3"),            //欠席３
                             "ATTEND_TOTAL"     =>  VARS::post("ATTEND_TOTAL"),       //欠席合計
                             "SENBATU1_SCHOOL"  =>  VARS::post("SENBATU1_SCHOOL"),    //選抜Ⅰ学校
                             "SENBATU1_MAJOR"   =>  VARS::post("SENBATU1_MAJOR"),     //選抜Ⅰ科
                             "SENBATU2_SCHOOL"  =>  VARS::post("SENBATU2_SCHOOL"),    //選抜Ⅱ学校
                             "SENBATU2_MAJOR"   =>  VARS::post("SENBATU2_MAJOR"),     //選抜Ⅱ科
                             "SCHOLARSHIP"      =>  VARS::post("SCHOLARSHIP"),        //奨学生
                             "RECOM_FLG"        =>  VARS::post("RECOM_FLG"),          //クラブ
                             "RECOM_REMARK"     =>  VARS::post("RECOM_REMARK"),       //推薦備考
                             "REMARK"           =>  VARS::post("REMARK"),             //備考
                             "NANKAN_FLG"       =>  VARS::post("NANKAN_FLG"),         //難関コース希望
                            );
    }

    //入力項目チェック
    function validateInput()
    {

        //DB接続
        $db = Query::dbCheckOut();

        //文字数チェック
        if (strlen($this->field["NAME"]) > 60) {
            $this->setWarning("MSG915","氏名は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["NAME_KANA"]) > 120) {
            $this->setWarning("MSG915","氏名カナは120バイトまでです。");
            return false;
        }
        if (strlen($this->field["FS_CD"]) > 7) {
            $this->setWarning("MSG915","出身学校コードは7バイトまでです。");
            return false;
        }
        if (strlen($this->field["SENBATU1_SCHOOL"]) > 24) {
            $this->setWarning("MSG915","選抜Ⅰ受験校は24バイトまでです。");
            return false;
        }
        if (strlen($this->field["SENBATU1_MAJOR"]) > 24) {
            $this->setWarning("MSG915","選抜Ⅰ科は24バイトまでです。");
            return false;
        }
        if (strlen($this->field["SENBATU2_SCHOOL"]) > 24) {
            $this->setWarning("MSG915","選抜Ⅱ受験校は24バイトまでです。");
            return false;
        }
        if (strlen($this->field["SENBATU2_MAJOR"]) > 24) {
            $this->setWarning("MSG915","選抜Ⅱ科は24バイトまでです。");
            return false;
        }
        if (strlen($this->field["RECOM_REMARK"]) > 150) {
            $this->setWarning("MSG915","推薦備考は150バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMARK"]) > 150) {
            $this->setWarning("MSG915","備考は150バイトまでです。");
            return false;
        }

        $query = knjl211rQuery::checkSchool($this->field["FS_CD"]);
        $existSchool = $db->getOne($query);
        if ($existSchool == 0) {
            $this->setWarning("出身学校コードが存在しません。");
            return false;
        }

        //DB切断
        Query::dbCheckIn($db);
        return true;
    }

    //更新処理
    function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl211rQuery::getUpdateQuery($this);
            if ($this->cmd == "update") {
                $this->setMessage("MSG201");
            }
        }
        return;
    }

    //削除処理
    function &getDeleteModel()
    {
        knjl211rQuery::getDeleteQuery($this);
        $this->setMessage("MSG201");
    }
 
}
?>