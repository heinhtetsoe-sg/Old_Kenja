<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjd654aModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjd654aModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "TESTKINDCD"       =>  VARS::post("TESTKINDCD"),       //テスト種別
                             "GROUP"            =>  VARS::post("GROUP"),            //クラス別、コース別、講座グループ別
                             "FORM_SELECT"      =>  VARS::post("FORM_SELECT"),      //最大科目数
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
    }

    function getDownloadModel()
    {
        $lf = "\n";
        //CSVデータ抽出処理
        //DB接続
        $db = Query::dbCheckOut();

        $dataContents = ""; //CSVデータ出力用
        $data = false;

        //学期名
        $semesterName = "";
        $query = knjd654aQuery::getSemester($this->field["SEMESTER"]);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $semesterName = $row["LABEL"];
        }
        $result->free();

        //テスト種別名
        $testkindName = '';
        $query = knjd654aQuery::getTestKind($this->field["SEMESTER"], $this->field["TESTKINDCD"]);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $testkindName = $row["NAME"];
        }
        $result->free();
        $title = sprintf("%d",$this->field["GRADE"]).'学年　'.$semesterName."　".$testkindName."クラス別平均点一覧表";

        $courses = array();
        if ($this->field["GROUP"] == "2") {
            $query = knjd654aQuery::getCourse($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $courses[] = array("CD" => $row["COURSE"], "NAME" => $row["NAME"]);
            }
            $result->free();
        } else {
            $courses[] = array("COURSE" => "", "NAME" => "");
        }

        $chairSubclasses = array();
        $query = knjd654aQuery::getChairSubclassList($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!$chairSubclasses[$row["SUBCLASSCD"]]) $chairSubclasses[$row["SUBCLASSCD"]] = array();
            $chairSubclasses[$row["SUBCLASSCD"]][] = $row["COURSE"];
        }

        foreach ($courses as $course) {
            if ($course["NAME"]) {
                $dataContents .= $course["NAME"].$lf;
            }
            $pgs = array();
            $query = knjd654aQuery::getPrintGroup($this, $course["CD"]);
            $result = $db->query($query);
            $courses = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $pgs[] = array("DIV" => "2", "CODE" => $row["CODE"], "NAME" => $row["NAME"], "LAST" => 0, "COURSE" => array());
                $courses[] = $row["COURSE"];
            }
            $result->free();
            if ($course["CD"]) {
                $pgs[] = array("DIV" => "3", "CODE" => "000".$course["CD"], "NAME" => "全体", "LAST" => 1, "COURSE" => $courses);
            } else {
                $pgs[] = array("DIV" => "1", "CODE" => "00000000000", "NAME" => "全体", "LAST" => 1, "COURSE" => $courses);
            }

            $subclasses = array();
            $query = knjd654aQuery::getSubclassList($this, $course["CD"]);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $subclasses[] = array("CD" => $row["SUBCLASSCD"], "NAME" => $row["SUBCLASSABBV"]);
            }
            $subclasses[] = array("CD" => "333333", "NAME" => "３教科平均");
            $subclasses[] = array("CD" => "555555", "NAME" => "５教科平均");
            $subclasses[] = array("CD" => "999999", "NAME" => "全教科平均");

            $averages = array();
            $query = knjd654aQuery::getAverageList($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $key = $row["AVG_DIV"]."-".$row["CODE"]."-".$row["SUBCLASSCD"];
                $averages[$key] = round($row["AVG"], 1);
            }

            $subclassMap = array();
            $maxcol = 27;
            $colsubclass = array();
            $col = 0;
            foreach ($subclasses as $subclass) {
                $subclassMap[$subclass["CD"]] = $subclass;
                if ($subclass["CD"] == "333333" || $subclass["CD"] == "555555" || $subclass["CD"] == "999999") {
                } else {
                    $colsubclass[$col] = $subclass["CD"];
                    $col++;
                }
            }
            for ($c = $col; $c < $maxcol; $c++) {
                $colsubclass[$c] = "";
                $col++;
            }
            $colsubclass[$col] = "333333";
            $col++;
            $colsubclass[$col] = "555555";
            $col++;
            $colsubclass[$col] = "999999";
            $col++;

            $dataContents .= "クラス＼科目,";
            foreach ($colsubclass as $i => $cd) {
                $subclass = $subclassMap[$cd];
                $dataContents .= $subclass["NAME"].",";
            }
            $dataContents .= $lf;

            // クラス別平均点
            foreach ($pgs as $printGroup) {
                $dataContents .= $printGroup["NAME"]."　平均点,";

                foreach ($colsubclass as $i => $cd) {
                    $subclass = $subclassMap[$cd];
                    $key = "";
                    if ($printGroup["LAST"] == '1') {
                        $courses = $chairSubclasses[$subclass["CD"]] ? $chairSubclasses[$subclass["CD"]] : array();
                        // 講座グループに登録されている講座の名簿のコースは、科目は全体平均点を表示しない
                        $print = true;
                        foreach ($printGroup["COURSE"] as $c) {
                            if (in_array($c, $courses)) {
                                $print = false;
                            }
                        }
                        if ($print) {
                            $key = $printGroup["DIV"]."-".$printGroup["CODE"]."-".$subclass["CD"];
                        }
                    } else {
                        $key = $printGroup["DIV"]."-".$printGroup["CODE"]."-".$subclass["CD"];
                    }
                    $dataContents .= $averages[$key].",";
                    $data = true;
                }
                $dataContents .= $lf;
            }
            $dataContents .= $lf;
        }

        //DB切断
        Query::dbCheckIn($db);
        $contents  = "";
        $contents .= $title.$lf;
        $contents .= $dataContents.$lf;
        if ($data){
            $filename = sprintf("%s.csv",$title);
            common::downloadContents($contents, $filename);
            return true;
        }else{
            $this->setWarning("MSG303");
            return false;
        }
    }//getDownloadModelの綴り

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
