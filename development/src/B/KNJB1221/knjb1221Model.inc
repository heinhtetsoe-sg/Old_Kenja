<?php

require_once('for_php7.php');

class knjb1221Model extends Model
{
    var $field = array();
    var $cmd;
    var $selectdata;
    var $selectdataText;

    function knjb1221Model()
    {
    }

    function init()
    {
        $this->field = array(
            "COURSE"              => VARS::post("COURSE"),              //コース
            "GRADE_HR_CLASS"      => VARS::post("GRADE_HR_CLASS"),      //年組
            "STUDENT"             => VARS::post("STUDENT"),             //コピー元生徒
            "COPY_CHAIR_DATE"     => VARS::post("COPY_CHAIR_DATE"),     //コピー元講座日付
            "TRGT_GRADE_HR_CLASS" => VARS::post("TRGT_GRADE_HR_CLASS"), //更新対象の年組
            "DATE"                => VARS::post("DATE"),                //開始日
        );

        if ($this->cmd == "changeCourse") {
            $this->field["GRADE_HR_CLASS"] = "";
            $this->field["STUDENT"] = "";
            $this->field["TRGT_GRADE_HR_CLASS"] = "";
        }

        if ($this->cmd == "changeHr") {
            $this->field["STUDENT"] = "";
            $this->field["TRGT_GRADE_HR_CLASS"] = "";
        }

        $this->cmd                 = VARS::request("cmd");
        $this->selectdataStd       = VARS::post("selectdataStd");
        $this->selectdataStdText   = VARS::post("selectdataStdText");
        $this->selectdataChair     = VARS::post("selectdataChair");
        $this->selectdataChairText = VARS::post("selectdataChairText");

        $db = Query::dbCheckOut();

        $row = $db->getRow(knjb1221Query::getSemesterRange(), DB_FETCHMODE_ASSOC);
        $this->sdate = $row["SDATE"];
        $this->edate = $row["EDATE"];

        Query::dbCheckIn($db);

        //プロパティファイルの読み込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //更新
    function &getUpdateModel() {
        knjb1221Query::getUpdateQuery($this);
        $this->setMessage("MSG202");
        return;
    }

}
?>