<?php

require_once('for_php7.php');

class knja263Model extends Model {
    var $grade;
    var $cmd;
    var $examyear;
    var $recordTableDiv;
    var $schoolCd;
    var $maxSemester;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    function knja263Model()
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

        $this->cmd   = VARS::request("cmd");
        $this->grade = VARS::post("GRADE");
        // 成績テーブルを判断するためのフラグを取得(法政・自修館・その他)
        $rtnRow = knja263Query::getNameMst();
        $this->recordTableDiv       = $rtnRow["NAMESPARE1"];// 1:RECORD_RANK_DAT, null:RECORD_DAT
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
                        "NAME"            => "氏名漢字",
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
                        "AVG_ASSEC"       => "評定平均",
                        "AVG_RECORD"      => "成績平均"
                        );

        $contents = implode($header, ",")."\n";
        $data = false;

        $db = Query::dbCheckOut();

        $schoolDiv = $db->getOne(knja263Query::getSchoolDiv());

        //SQL文発行
        $query = knja263Query::selectCvsQuery($this);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){

            $asses = "";
            $record = "";
            //学年制
            if ($schoolDiv == "0") {
                if ($this->recordTableDiv == "1" || $this->Properties["useTestCountflg"] == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
                    $asses = $row["LEVEL_AVG"];
                } else {
                    if ($row["LEVEL_CNT"] > 0) {
                        $asses = round((int)$row["LEVEL_SUM"]/(int)$row["LEVEL_CNT"],1);
                    }
                }
            }
            //データセット
            $contents .= $this->examyear.","
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
                        .$asses.","
                        .$record." \n";
            $data = true;
        }
        $result->free();

        if ($this->Properties["useKeepGrade"] == "1" && $schoolDiv == "1") {
            $gradename = "";
        } else {
            //進級学年名称取得
            $gradename = "（新".$db->getOne(knja263Query::getGradeName($this))."）";
        }

        Query::dbCheckIn($db);

        if ($data) {
            $filename = $this->examyear."_進級者成績".$gradename.".csv";
            //ファイルダウンロード
            common::downloadContents($contents, $filename);

            return true;
        } else {
            $this->setWarning("MSG303");    //データは存在していません。
            return false;
        }
    }
}
?>
