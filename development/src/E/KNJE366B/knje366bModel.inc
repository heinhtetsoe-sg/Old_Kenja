<?php

require_once('for_php7.php');


class knje366bModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード
    var $selectdata;

    function knje366bModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //職員コード
    }

    function init() {
        $this->field = array("YEAR"         =>  VARS::post("YEAR")            //年度
                            , "SEMESTER"     =>  VARS::post("SEMESTER")       //学期
                            ,  "OUT_DIV"      =>  VARS::post("OUT_DIV")       //対象ラジオボタン 
                            ,  "GOUHI"        =>  VARS::post("GOUHI")         //対象（合否・進路状況）コンボ
                            ,  "COMPANYCD"    =>  VARS::post("COMPANYCD")     //業者コードコンボ
                            ,  "MOCKCD"       =>  VARS::post("MOCKCD")        //模試コードコンボ
                            ,  "DBNAME"       =>  VARS::post("DBNAME")        //DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");
        $this->selectdata  = VARS::post("SELECTDATA");
        $this->selecttypedata = VARS::post("SELECTTYPEDATA");
        $this->selectdatatypecnt = VARS::post("SELECTDATA_TYPE_CNT");
    }

    function getDownloadModel() {
        return $this->csvUniversityResult();
    }

    function csvUniversityResult() {

        //DB接続
        $db = Query::dbCheckOut();

        $contents = "";
        //ヘッダ
        $header = array(
              "STAT_NAME"        => "学校名"
            , "FACULTYNAME"      => "学部名"
            , "DEPARTMENTNAME"   => "学科名"
            , "HOWTOEXAM_NAME"   => "受験方式"
            , "DECISION_NAME"    => "合否"
            , "PLANSTAT_NAME"    => "進路状況"
            , "HR_NAME_ATTENDNO" => "年組番"
            , "NAME"             => "氏名"
            // , "SCHREGNO"         => "学籍番号"
        );

        $header2 = array();
        //進路分析資料 項目
        if ($this->field["OUT_DIV"] == "2") {
            $header["RANK1M_E"] = "１学期中間評価";
            $header["RANK1M_R"] = "１学期中間評定";
            $header["RANK1E_E"] = "１学期期末評価";
            $header["RANK1E_R"] = "１学期期末評定";
            $header["RANK2M_E"] = "２学期中間評価";
            $header["RANK2M_R"] = "２学期中間評定";
            $header["RANK2E_E"] = "２学期期末評価";
            $header["RANK2E_R"] = "２学期期末評定";
            $header["RANK2E_E"] = "学年末評価";
            $header["RANK2E_R"] = "学年末評定";
            $header["RANK9E_E"] = "総合成績";
            $header["RANK9E_R"] = "学年順位";

            //模試コード(ベネッセ用)
            $header2["MOCK_KYOUZAINAME"]   = "模試名称";
            $nameRow = $db->getRow(knje366bQuery::getNameMst($model, 'A023', 'H'), DB_FETCHMODE_ASSOC);
            $nameMstGrade = $nameRow["NAMESPARE2"];
            $subclassCnt = $db->getOne(knje366bQuery::getMockSubclassCnt($this, $nameMstGrade));
            for ($i=0; $i < $subclassCnt; $i++) { 
                $header2["MOCK_SUBCLASS_NAME"."_".$i] = "科目名";
                $header2["MOCK_SCORE"."_".$i]         = "点数";
                $header2["MOCK_GTZ"."_".$i]           = "GTZ";
            }
        }
        $contents .= implode($header, ",");
        if (get_count($header2) > 0) {
            $contents .= ",".implode($header2, ",");
        }
        $contents .= "\n";

        //クラス一覧
        $hrClassList = array();
        if ($this->selectdata) {
            $hrClassList = explode(',', $this->selectdata);
        }

        //対象(合否・進路状況)
        $decisionList = array();
        $planstat = array();
        $gouhi = explode('-', $this->field["GOUHI"]);
        if ($gouhi[1] == 'MIX') {
            $query = knje366bQuery::getNameMst2($gouhi[0], $gouhi[2]);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($gouhi[0] == 'E005') {
                    $decisionList[] = $row['VALUE'];
                } else if ($gouhi[0] == 'E006') {
                    $planstat[] = $row['VALUE'];
                }
            }
            $result->free();
        } else {
            if ($gouhi[0] == 'E005') {
                $decisionList[] = $gouhi[1];
            } else if ($gouhi[0] == 'E006') {
                $planstat[] = $gouhi[1];
            }
        }

        //種別（設置区分）一覧
        $typeList = array();
        if ($this->selecttypedata) {
            $typeList = explode(',', $this->selecttypedata);
        }

        //SQL文発行
        $data = false;
        $query = knje366bQuery::getCsvQuery($this, $hrClassList, $decisionList, $planstat, $typeList);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",", ".", $row);

            //既卒生
            if (substr($row["GRADE"], 0, 2) == "ZZ") {
                //学年
                list ($dummyG, $year, $grade) = explode('-', $row["GRADE"]);
                $row["GRADE"]  = "";
                if ($year) $row["GRADE"] .= $year.'年度卒';
                if ($grade) $row["GRADE"] .= $grade;
                //組
                list ($dummyH, $hr_class) = explode('-', $row["HR_CLASS"]);
                $row["HR_CLASS"]  = "";
                if ($hr_class) $row["HR_CLASS"] .= $hr_class;
                //出席番号
                $row["ATTENDNO"] = "";
            }

            $sep = "";
            foreach ($header as $field => $name) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }

            if ($this->field["OUT_DIV"] == "2") {
                $kyouzaiRow = $db->getRow(knje366bQuery::getNameMst($model, 'A023', 'H'), DB_FETCHMODE_ASSOC);
                $kyouzaiCd = substr($kyouzaiRow['NAMESPARE2'], -1);
                //教材名取得
                $kyouzaiName = $db->getOne(knje366bQuery::getMockKyouzaiName($this, $kyouzaiCd));
                $contents .= ",".$kyouzaiName;
                $mockSubclassCnt = 0;
                //模試情報追加
                $query = knje366bQuery::getMockRankRangeDat($this, $row["SCHREGNO"]);
                $result2 = $db->query($query);
                while ($row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $contents .= ",".$row2["MOCK_SUBCLASS_NAME"];
                    $contents .= ",".$row2["SCORE"];
                    $contents .= ",".$row2["GTZ"];

                    $mockSubclassCnt++;
                }
                //模試科目分のカンマを追加
                for ($i=$mockSubclassCnt; $i < $subclassCnt; $i++) { 
                    $contents .= ",,,";
                }
                $result2->free();
            }

            $contents .= "\n";
            $data = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);
        //ＣＳＶ出力
        if ($data) {
            $nendo = $this->field['YEAR']."年度_";
            $title = $nendo ."大学別合否結果一覧";
            if ($this->field["OUT_DIV"] == "2") {
                $title = $nendo ."進路分析資料";
            }
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

}
?>
