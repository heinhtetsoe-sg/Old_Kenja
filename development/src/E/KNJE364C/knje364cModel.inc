<?php

require_once('for_php7.php');

class knje364cModel extends Model {
    var $field = array();
    var $cmd;
    var $year;
    var $selectdata;
    var $selectdataText;

    function knje364cModel() {}

    function init() {
        $this->cmd          = VARS::request("cmd");

        $this->year = CTRL_YEAR;
        $this->semester = CTRL_SEMESTER;

        $this->field = array(
            "ENTRYDATE"         =>  VARS::post("ENTRYDATE"),         //登録日
            "COURSE_KIND"       =>  VARS::post("COURSE_KIND"),       //進路種別
            "QUESTIONNAIRECD"   =>  VARS::post("QUESTIONNAIRECD"),   //調査名
            "SCHOOL_KIND"       =>  VARS::post("SCHOOL_KIND"),       //校種
        );

        $this->selectdata       = VARS::post("selectdata");
        $this->selectdataText   = VARS::post("selectdataText");

        //プロパティファイルの読み込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //CSV出力
    function getCsvOutputModel() {

        $contents  = "";    //クリア

        //DB接続
        $db = Query::dbCheckOut();

        $count = array();

        $selectdataArray = explode(",", $this->selectdata);
        foreach ($selectdataArray as $gradeHrClass) {
            //学校系列
            $schoolGroup = array();
            $query = knje364cQuery::getVNameMst($this, "Z010");
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $schoolGroup["SCHOOL_GROUP".$row["NAMECD2"]] = $row["NAME1"];
                $count["TOTAL"]["SCHOOL_GROUP".$row["NAMECD2"]] = 0;
                $count[1]["SCHOOL_GROUP".$row["NAMECD2"]] = 0;
                $count[2]["SCHOOL_GROUP".$row["NAMECD2"]] = 0;
            }
            $result->free();

            $query = knje364cQuery::getVNameMst($this, "E012");
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $schoolGroup["SCHOOL_GROUP".$row["NAMECD2"]] = $row["NAME1"];
                $count["TOTAL"]["SCHOOL_GROUP".$row["NAMECD2"]] = 0;
                $count[1]["SCHOOL_GROUP".$row["NAMECD2"]] = 0;
                $count[2]["SCHOOL_GROUP".$row["NAMECD2"]] = 0;
            }
            $result->free();

            //学部系列
            $facultyGroup = array();
            $query = knje364cQuery::getFacultyGroupMst();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $facultyGroup["FACULTY_GROUP".$row["FACULTY_GROUP"]] = $row["FACULTY_GROUPNAME"];
                $count["TOTAL"]["FACULTY_GROUP".$row["FACULTY_GROUP"]] = 0;
                $count[1]["FACULTY_GROUP".$row["FACULTY_GROUP"]] = 0;
                $count[2]["FACULTY_GROUP".$row["FACULTY_GROUP"]] = 0;
            }
            $result->free();

            //学科系列
            $departmentGroup = array();
            $query = knje364cQuery::getDepartmentGroupMst();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $departmentGroup["DEPARTMENT_GROUP".$row["DEPARTMENT_GROUP"]] = $row["DEPARTMENT_GROUPNAME"];
                $count["TOTAL"]["DEPARTMENT_GROUP".$row["DEPARTMENT_GROUP"]] = 0;
                $count[1]["DEPARTMENT_GROUP".$row["DEPARTMENT_GROUP"]] = 0;
                $count[2]["DEPARTMENT_GROUP".$row["DEPARTMENT_GROUP"]] = 0;
            }
            $result->free();

            $header_data = true;   //出力フラグ

            $data = "";
            $header1 = "";
            $header2 = "";
            $footer1 = "";
            $footer2 = "";

            $hrClassName = $db->getOne(knje364cQuery::getHrClassName($this, $gradeHrClass));

            $setData = array();
            $query = knje364cQuery::getCsvDat($this, $gradeHrClass);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $setData["ATTENDNO"] = $row["ATTENDNO"];
                $setData["NAME"] = $row["NAME"];

                foreach ($schoolGroup as $key => $name) {
                    if ("SCHOOL_GROUP".$row["SCHOOL_GROUP1"] == $key) {
                        $setData[$key] = "1";
                        $count["TOTAL"][$key]++;
                        $count[$row["SEX"]][$key]++;
                    } else {
                        $setData[$key] = "";
                    }
                }

                foreach ($facultyGroup as $key => $name) {
                    if ("FACULTY_GROUP".$row["FACULTY_GROUP1"] == $key) {
                        $setData[$key] = "1";
                        $count["TOTAL"][$key]++;
                        $count[$row["SEX"]][$key]++;
                    } else {
                        $setData[$key] = "";
                    }
                }

                foreach ($departmentGroup as $key => $name) {
                    if ("DEPARTMENT_GROUP".$row["DEPARTMENT_GROUP1"] == $key) {
                        $setData[$key] = "1";
                        $count["TOTAL"][$key]++;
                        $count[$row["SEX"]][$key]++;
                    } else {
                        $setData[$key] = "";
                    }
                }

                $setData["SCHOOL_CD1"] = $row["SCHOOL_CD1"];
                $setData["SCHOOL_NAME1"] = $row["SCHOOL_NAME1"];
                $setData["SCHOOL_GROUP1"] = $row["SCHOOL_GROUP1"];
                $setData["SCHOOL_GROUP_NAME1"] = $row["SCHOOL_GROUP_NAME1"];
                $setData["FACULTY_GROUP1"] = $row["FACULTY_GROUP1"];
                $setData["FACULTY_GROUPNAME1"] = $row["FACULTY_GROUPNAME1"];
                $setData["SCHOOL_CD2"] = $row["SCHOOL_CD2"];
                $setData["SCHOOL_NAME2"] = $row["SCHOOL_NAME2"];

                for ($i = 3; $i <= 6; $i++) {
                    $query2 = knje364cQuery::getCourseHopeDetailDat($this, $row["SEQ"], $row["SCHREGNO"], $i);
                    $row2 = $db->getRow($query2, DB_FETCHMODE_ASSOC);

                    $setData["SCHOOL_CD".$i] = $row2["SCHOOL_CD"];
                    $setData["SCHOOL_NAME".$i] = $row2["SCHOOL_NAME"];
                }
                $data .= implode(",", $setData)."\n";
            }
            $header1  = ",".$hrClassName.",".implode(",", $schoolGroup).",".implode(",", $facultyGroup).",".implode(",", $departmentGroup).",大学コード①,大学①,学校区分コード,学校区分,学部系統コード,学部系統,大学コード②,大学②,大学コード③,大学③,大学コード④,大学④,大学コード⑤,大学⑤,大学コード⑥,大学⑥\n";
            $header2 = "出席番号,クラス人数,".implode(",", $count["TOTAL"])."\n";
            $footer1 = ",男子合計,".implode(",", $count[1])."\n";
            $footer2 = ",女子合計,".implode(",", $count[2])."\n";

            $contents .= $header1.$header2.$data.$footer1.$footer2."\n";
        }

        Query::dbCheckIn($db);

        //CSV出力
        if ($header_data) {
            $couse = "進路調査票";

            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
?>