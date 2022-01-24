<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl315rModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $fixApplicantDiv;  //入試制度(ログイン校種により固定)
        
    /**
     * メンバ変数の初期化
     */
    function knjl315rModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear = CTRL_YEAR+1;
    }
    function init()
    {               
        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),   //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV")         //入試区分
                     );
        $this->cmd         = VARS::request("cmd");
        $this->fixApplicantDiv = '1';
        if (SCHOOLKIND == 'J') {
                $this->fixApplicantDiv = '2';
        }
    }
    
    //CSV出力処理
    function getDownloadModel() {
        //変数定義
        $comma = ","; //カンマ
        $lf = "\n";   //改行
        //DB接続
        $db = Query::dbCheckOut();
        $dataContents = "";

        $query = knjl315rQuery::getExamCourseMst($this->ObjYear, $this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!$courses[$row["DESIREDIV"]]) {
                $courses[$row["DESIREDIV"]] = array();
            }
            $courses[$row["DESIREDIV"]]["DESIREDIV"] = $row["DESIREDIV"];
            $courses[$row["DESIREDIV"]]["COURSECD"] = $row["COURSECD"];
            $courses[$row["DESIREDIV"]]["MAJORCD"] = $row["MAJORCD"];
            $courses[$row["DESIREDIV"]]["EXAMCOURSECD"] = $row["EXAMCOURSECD"];
            $courses[$row["DESIREDIV"]]["WISHNO"] = $row["WISHNO"];
            if (!$courses[$row["DESIREDIV"]]["EXAMCOURSE_MARKS"]) {
                $courses[$row["DESIREDIV"]]["EXAMCOURSE_MARKS"] = array();
            }
            $courses[$row["DESIREDIV"]]["EXAMCOURSE_MARKS"][] = $row["EXAMCOURSE_MARK"];
            if (!$courses[$row["DESIREDIV"]]["FIRST"]) {
                $courses[$row["DESIREDIV"]]["FIRST"] = $row["EXAMCOURSE_MARK"];
            }
            $courses[$row["DESIREDIV"]]["LAST"] = $row["EXAMCOURSE_MARK"];
        }

        $testDivName1 = $db->getOne(knjl315rQuery::getNameMstValue($this->ObjYear, "L004", $this->field["TESTDIV"], "NAME1"));
        $testDivAbbv3 = $db->getOne(knjl315rQuery::getNameMstValue($this->ObjYear, "L004", $this->field["TESTDIV"], "ABBV3"));

        $data = false;
        foreach ($courses as $desireDiv => $course) {
            $perfect = 90;
            $queryPerfect = knjl315rQuery::getCoursePefect($this->ObjYear, $this, $course["COURSECD"].$course["MAJORCD"].$course["EXAMCOURSECD"]);
            $result = $db->query($queryPerfect);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $perfect += $row["PERFECT"];
            }

            foreach (array(true, false) as $naidaku) {
                $marks = "";
                $com = "";
                foreach ($course["EXAMCOURSE_MARKS"] as $mark) {
                    $marks .= $com.$mark;
                    $com = "、";
                }
                $title = "";
                if ($naidaku) {
                    $title = $testDivAbbv3 ."あり（".$course["LAST"]."内諾".$marks."受験）";
                } else {
                    $title = $testDivAbbv3 ."なし（".$marks."受験）";
                }
                $dataContents .= $lf;
                $dataContents .= $title.$lf;
                $dataContents .= "得点".$comma."人数".$comma."累計".$lf;

                $scores = array();
                $queryScore = knjl315rQuery::getScoreList($this->ObjYear, $this, $desireDiv, $naidaku, true);
                $result = $db->query($queryScore);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $scores[] = $row["SCORE"];
                }
                $scores2 = array();
                $queryScore2 = knjl315rQuery::getScoreList($this->ObjYear, $this, $desireDiv, $naidaku, false);
                $result = $db->query($queryScore2);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $scores2[] = $row["SCORE"];
                }
                $total = 0;
                $scoreDistList = knjl315rQuery::getScoreDistList($this->ObjYear, $this, $desireDiv, $naidaku, $perfect, $scores);
                foreach ($scoreDistList as $scoreDist) {
                    if ($perfect == $scoreDist["LOWER"]) {
                        $scoreRange = $scoreDist["LOWER"];
                    } else {
                        $scoreRange = $scoreDist["UPPER"]."～".$scoreDist["LOWER"];
                    }
                    $count = $scoreDist["COUNT"];
                    $total += $count;
                    $dataContents .= $scoreRange.$comma.$count.$comma.$total.$lf;
                }
                $dataContents .= $comma."（受験者数） ". $comma.get_count($scores).$lf;
                $dataContents .= $comma."（未受験者数） ". $comma.get_count($scores2).$lf;
                $dataContents .= $lf;
            }
            $data = true;
        }

        //DB切断
        Query::dbCheckIn($db);
        //CSV出力
        //タイトル
        $nendo = common::DateConv1($this->ObjYear."/04/01",10);
        $date = common::DateConv1(str_replace("-", "/", CTRL_DATE), 0);
        $time = "　".date('H時i分現在');
        $title = "";
        $title = $nendo.'　'.$testDivName1.'得点分布表,,,,'.$date.$time.$lf;
        //サブタイトル
        $subtitle = "";
        //$subtitle  =  $comma.$comma.$comma.$comma.$comma."";
        //$subtitle .=  $comma.$comma.$comma.$this->wareki_date.$lf;
        
        $contents  = "";
        $contents .= $title;
        $contents .= $subtitle;
        $contents .= $headerContents;
        $contents .= $dataContents;
        if ($data) {
            $filename = sprintf("%s.csv", $nendo.'　'.$testDivName1.'得点分布表');
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
    
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
