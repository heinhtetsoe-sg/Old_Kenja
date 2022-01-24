<?php

require_once('for_php7.php');

class knja042Model extends Model {
    var $examcourseall = array();
    var $grade;
    var $cmd;
    var $examyear;
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    function knja042Model()
    {
        $this->examyear = CTRL_YEAR + 1;
    }

    function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;
        $this->getPropertiesAll(); //プロパティファイル

        $this->cmd     = VARS::request("cmd");
        $this->grade   = VARS::post("GRADE");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    function getDownloadModel()
    {

        $header = array("YEAR"            => "年度",
                        "SCHREGNO"        => "学籍番号",
                        "SEMESTER"        => "学期",
                        "NAME"            => "氏名",
                        "NAME_KANA"       => "氏名かな",
                        "SEX"             => "性別",
                        "GRADE"           => "学年",
                        "HR_CLASS"        => "組",
                        "ATTENDNO"        => "出席番号",
                        "COURSECD"        => "課程コード",
                        "MAJORCD"         => "学科コード",
                        "COURSECODE"      => "コースコード",
                        "REMAINGRADE_FLG" => "留年フラグ",
                        "OLD_SCHREGNO"    => "旧学籍番号",
                        "OLD_GRADE"       => "旧学年",
                        "OLD_HR_CLASS"    => "旧組",
                        "OLD_ATTENDNO"    => "旧出席番号",
                        "SCORE"           => "成績"
                        );

        $contents = implode($header, ",")."\n";
        $data = false;

        $db = Query::dbCheckOut();

        //SQL文発行
        $query = knja042Query::selectCvsQuery($this);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){

            //データセット
            $contents .= $row["YEAR"].","
                        .$row["SCHREGNO"].","
                        .$row["SEMESTER"].","
                        .$row["NAME"].","
                        .$row["NAME_KANA"].","
                        .$row["SEX"].","
                        .$row["GRADE"].","
                        .$row["HR_CLASS"].","
                        .$row["ATTENDNO"].","
                        .$row["COURSECD"].","
                        .$row["MAJORCD"].","
                        .$row["COURSECODE"].","
                        .$row["REMAINGRADE_FLG"].","
                        .$row["OLD_SCHREGNO"].","
                        .$row["OLD_GRADE"].","
                        .$row["OLD_HR_CLASS"].","
                        .$row["OLD_ATTENDNO"].","
                        .$row["SCORE"]." \n";
            $data = true;
        }
        $result->free();

        //学年
        if ($this->grade == "99") {
            $gradename = '全て';
        } else {
            $gradename = $db->getOne(knja042Query::getGrade($this, $this->grade));
        }

        Query::dbCheckIn($db);

        if ($data) {
            $filename = $this->examyear."_クラス編成結果（".$gradename."）.csv";
            //ファイルダウンロード
            common::downloadContents($contents, $filename);

            return true;
        }else{
            $this->setWarning("MSG303");    //データは存在していません。
            return false;
        }
    }
}
?>
