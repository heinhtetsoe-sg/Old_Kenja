<?php

require_once('for_php7.php');

class knjl090eModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $examType;          //
    var $examno;            //受験番号
    var $checkexam;
    var $field = array();           //フィールドデータ

    function knjl090eModel() {
    }

    function init() {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->examno);
            unset($this->year);
            unset($this->checkexam);
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        $this->cmd      = VARS::request("cmd");
        $this->year     = CTRL_YEAR + 1;
        $this->examType = '1';
        $this->shDivCnt = '6';
        $this->examno   = VARS::post("EXAMNO");

        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId     = VARS::get("SEND_PRGID");
            $this->sendAuth     = VARS::get("SEND_AUTH");
            $this->examno       = VARS::get("SEND_EXAMNO");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        //フィールド
        $this->field = array("APPLICANTDIV"         =>  VARS::post("APPLICANTDIV"),         //入試制度
                             "TESTDIV"              =>  VARS::post("TESTDIV"),              //入試区分
                             "TESTDIV1"             =>  VARS::post("TESTDIV1"),             //受験区分
                             "DESIREDIV"            =>  VARS::post("DESIREDIV"),            //出願コース
                             "SHDIV"                =>  VARS::post("SHDIV"),                //専併
                             "LOGIN_ID"             =>  VARS::post("LOGIN_ID"),             //ログインID

                             /* 志願者情報 */
                             "NAME"                 =>  VARS::post("NAME"),                 //氏名(志願者)
                             "NAME_KANA"            =>  VARS::post("NAME_KANA"),            //氏名カナ(志願者)
                             "SEX"                  =>  VARS::post("SEX"),                  //性別
                             "BIRTHDAY"             =>  VARS::post("BIRTHDAY"),             //生年月日
                             "FINSCHOOLCD"          =>  VARS::post("FINSCHOOLCD"),          //出身校（中学）
                             "FS_DAY"               =>  VARS::post("FS_DAY"),               //卒業年月
                             "FS_GRDDIV"            =>  VARS::post("FS_GRDDIV")             //卒業区分
                            );

        for ($i = 1; $i <= $this->shDivCnt; $i++) {
            $this->field["H_ENT_COURSE_".$i]    = VARS::post("H_ENT_COURSE_".$i);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    //入力項目チェック
    function validateInput() {
        //DB接続
        $db = Query::dbCheckOut();

        //空文字チェック
        if (!($this->field["APPLICANTDIV"])) {
            $this->setWarning("MSG301", "( 入試制度 )");
            return false;
        }
        if (!($this->field["TESTDIV"])) {
            $this->setWarning("MSG301", "( 入試区分 )");
            return false;
        }
        if (!($this->examno)) {
            $this->setWarning("MSG301", "( 受験番号 )");
            return false;
        }

        //入学コース選択は一つor null のみ
        $entCourseCnt = 0;
        for ($i = 1; $i <= $this->shDivCnt; $i++) {
            if ($this->field["H_ENT_COURSE_".$i] != '') {
                $entCourseCnt++;
            }
        }
        if ($entCourseCnt > 1) {
            $this->setWarning("MSG203", "入学コースは一つしか選択出来ません。");
            return false;
        }

        //キー値変更チェック
        if ($this->cmd != "add" && $this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新は検索を実行してから行ってください。");
            return false;
        }

        //存在チェック
        $cnt = $db->getOne(knjl090eQuery::getExistdataCnt($this));
        if ($cnt == 0 && ($this->cmd == "update" || $this->cmd == "back" || $this->cmd == "next")) {
            $this->setWarning("MSG303", '受験番号');
            return false;
        }

        //DB切断
        Query::dbCheckIn($db);
        return true;
    }

    //更新処理
    function &getUpdateModel() {
        if ($this->validateInput()) {
            $result = knjl090eQuery::getUpdateQuery($this);
            if ($this->cmd == "update") {
                $this->setMessage("MSG201");
            }
        }
        return;
    }
}
?>