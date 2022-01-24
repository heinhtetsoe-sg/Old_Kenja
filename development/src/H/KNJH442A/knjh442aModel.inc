<?php

/**
 * 定数定義
*/

class knjh442aModel extends Model
{
    public $field      = array();
    public $cmd;
    public $control;   //コントロールマスタ
    public $categorySelected;
    public $percentage;
    public $percentage2;
        
    /**
     * メンバ変数の初期化
     */
    public function knjh442aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("YEAR"         =>  VARS::post("YEAR"),         //年度
                             "TITLEDIV"     =>  VARS::post("TITLEDIV"),     //指定校推薦、日大付属特別選抜
                             "OUTPUT"       =>  VARS::post("OUTPUT"),       //学級別、コース別
                             "GRADE"        =>  VARS::post("GRADE"),        //学年
                             "DBNAME"       =>  VARS::post("DBNAME")        //DB変更に備えて
                     );

        $this->categorySelected = explode(",", VARS::post("HID_CATEGORY_SELECTED"));

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
    }

    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }

    /***********/
    /* CSV出力 */
    /***********/
    public function getCsvModel()
    {

        //AFT_RECOMMENDATION_RANK_HEAD_DAT の取得
        $db = Query::dbCheckOut();
        $query = knjh442aQuery::getAftRecommendationRankHead($this);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $this->percentage = $row["PERCENTAGE"];
        $this->percentage2 = 100 - $this->percentage;
        //DB切断
        Query::dbCheckIn($db);
        
        //初期化
        $contents = "";

        $data = false;
        foreach ($this->categorySelected as $key => $value) {
            $contents .= $this->setContents($value, $data);
            $contents .= "\n"; //改行
            $contents .= "\n"; //改行
        }

        //CSV出力
        if ($data) {
            $title = $this->field["YEAR"]."年度 指定校推薦";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    //データ出力
    public function setContents($value, &$data)
    {
        //DB接続
        $db = Query::dbCheckOut();

        //初期化
        $contents = "";

        //1行目
        $contents .= $this->field["YEAR"]."年度 指定校推薦";
        $contents .= ",,,,,,,,,,";
        $contents .= $this->field["TITLEDIV"] == "1" ? "学級別学生成績一覧表" : "コース別学生成績一覧表";
        $contents .= ",,,,,,,";
        // list($yy, $mm, $dd) = explode("-", CTRL_DATE);
        // list($hh, $mm2) = explode(":", "11:23");
        // $contents .= $yy."年".$mm."月".$dd."日 ".$hh."時".$mm2."分";
        list($yy, $mm, $dd) = explode("-", CTRL_DATE);
        $contents .= $yy."年".$mm."月".$dd."日 ";
        $contents .= "\n"; //改行

        //2行目
        $query = knjh442aQuery::getCertifSchoolDat($this);
        $schoolInfo = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $query = knjh442aQuery::getSchregRegdGdat($this);
        $gdat = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $contents .= $schoolInfo["SCHOOL_NAME"].",".$gdat["GRADE_NAME1"];
        $contents .= ",,,,,,,,,";
        $contents .= "【学内成績・成績】";
        $contents .= "\n"; //改行

        //明細(ヘッダ1行目)
        if ($this->field["OUTPUT"] == "1") {
            $query = knjh442aQuery::getSchregRegdHdat($this, $value);
            $hdat = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $contents .= $hdat["HR_NAMEABBV"].","; //年組名称
        } else {
            $contents .= "コース,";
        }
        $contents .= ",";
        $contents .= ",";
        $maxCol = 10;
        $col = 1;
        $perfect = array();
        $subclassArr = array();
        $query = knjh442aQuery::getSubclass($this, $value);
        $result = $db->query($query);
        while ($subclassRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($col > $maxCol) {
                break;
            }
            $contents .= "評".$subclassRow["SUBCLASSNAME"].",";
            $perfect[] = "5";
            $subclassArr[] = $subclassRow["SUBCLASSCD"];
            $col++;
        }
        $result->free();
        $contents .= "理科履修科目,";
        $contents .= "実国語,";
        $contents .= "実数学,";
        $contents .= "実英語,";
        $contents .= "評平均,";
        $contents .= "評".$this->percentage.",";
        $contents .= "実平均,";
        $contents .= "実".$this->percentage2.",";
        $contents .= "学内成績,,,";
        $contents .= "順位";
        $contents .= "\n"; //改行

        //明細(ヘッダ2行目)
        if ($this->field["OUTPUT"] == "1") {
            $contents .= "担任 "."仮担任氏名".",";
        } else {
            $query = knjh442aQuery::getCourseName($this, $value);
            $course = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $contents .= $course["COURSENAME"].","; //コース名称
        }
        $contents .= ",";
        $contents .= ",";
        $contents .= implode($perfect, ",").",";
        $contents .= ",";
        $contents .= "100,";
        $contents .= "100,";
        $contents .= "100,";
        $contents .= "5,";
        $contents .= $this->percentage.",";
        $contents .= "100,";
        $contents .= $this->percentage2.",";
        $contents .= "コース上位％,";
        $contents .= "学年上位％,";
        $contents .= "総合成績,";
        $contents .= "クラス,";
        $contents .= "コース,";
        $contents .= "学年";
        $contents .= "\n"; //改行

        //明細
        $query = knjh442aQuery::getSchregInfo($this, $value);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //生徒情報
            $contents .= $row["HR_CLASS"].",";
            $contents .= $row["ATTENDNO"].",";
            $contents .= $row["NAME"].",";

            //評定
            $query = knjh442aQuery::getReocrdRankSdiv($this, $row["SCHREGNO"]);
            $result2 = $db->query($query);
            $recordRankSdivArr = array();
            while ($Row = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
                $key = $Row["SUBCLASSCD"] == "999999" ? $Row["SUBCLASSCD"] : $Row["CLASSCD"]."-".$Row["SCHOOL_KIND"]."-".$Row["CURRICULUM_CD"]."-".$Row["SUBCLASSCD"];
                $recordRankSdivArr[$key] = $Row["SCORE"];
            }
            $result2->free();
            foreach ($subclassArr as $key => $subclassCd) {
                $contents .= $recordRankSdivArr[$subclassCd].",";
            }

            //理科履修科目
            $query = knjh442aQuery::getSubclassName($this, $row["SCHREGNO"], "05");
            $result2 = $db->query($query);
            while ($Row = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
                $contents .= $Row["SUBCLASSNAME"]." ";
            }
            $result2->free();
            $contents .= ",";

            //総合成績
            $query = knjh442aQuery::getAftSchregRecommendationRank($this, $row["SCHREGNO"]);
            $aftSchregRecommendationRank = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $contents .= $aftSchregRecommendationRank["MOCK_NATIONAL_LANGUAGE_AVG"].",";
            $contents .= $aftSchregRecommendationRank["MOCK_MATH_AVG"].",";
            $contents .= $aftSchregRecommendationRank["MOCK_ENGLISH_AVG"].",";
            $contents .= $aftSchregRecommendationRank["TEST_VALUATION_AVG"].",";
            $contents .= $aftSchregRecommendationRank["TEST_VALUATION_PERCENT_SCORE"].",";
            $contents .= $aftSchregRecommendationRank["MOCK_TOTAL_AVG"].",";
            $contents .= $aftSchregRecommendationRank["MOCK_TOTAL_PERCENT_SCORE"].",";

            //学内成績
            $courseRank = $aftSchregRecommendationRank["MOCK_TOTAL_SCORE_COURSE_RANK"];
            $gradeRank = $aftSchregRecommendationRank["MOCK_TOTAL_SCORE_GRADE_RANK"];
            $cnt = $aftSchregRecommendationRank["CNT"];
            if ($courseRank > 0 && $cnt > 0) {
                $courseTop = $courseRank / $cnt;
                $contents .= $courseTop; //コース上位%
            }
            $contents .= ",";
            if ($gradeRank > 0 && $cnt > 0) {
                $gradeTop = $gradeRank / $cnt;
                $contents .= $gradeTop; //学年上位%
            }
            $contents .= ",";
            $contents .= $aftSchregRecommendationRank["TOTAL_SCORE"].",";
            
            //順位
            $contents .= $aftSchregRecommendationRank["MOCK_TOTAL_SCORE_CLASS_RANK"].",";
            $contents .= $aftSchregRecommendationRank["MOCK_TOTAL_SCORE_COURSE_RANK"].",";
            $contents .= $aftSchregRecommendationRank["MOCK_TOTAL_SCORE_GRADE_RANK"].",";

            $contents .= "\n"; //改行
            $data = true;
        }
        $result->free();

        //クラス平均 or コース平均  評定
        $contents .= $this->field["OUTPUT"] == "1" ? "クラス平均," : "コース平均,";
        $contents .= ",";
        $contents .= ",";
        $avgArr = array();
        $kbn = $this->field["OUTPUT"] == "1" ? "CLASS" : "COURSE";
        $query = knjh442aQuery::getAvgRecordRank($this, $kbn, $value);
        $result = $db->query($query);
        while ($Row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $avgArr[$Row["SUBCLASSCD"]] = $Row["SCORE_AVG"];
        }
        $result->free();
        foreach ($subclassArr as $key => $subclassCd) {
            $contents .= $avgArr[$subclassCd].",";
        }
        $contents .= ",";

        //クラス平均 or コース平均  総合成績
        $query = knjh442aQuery::getAvgAftSchregRecommendationRank($this, $kbn, $value);
        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $contents .= $Row["MOCK_NATIONAL_LANGUAGE_AVG"].",";
        $contents .= $Row["MOCK_MATH_AVG"].",";
        $contents .= $Row["MOCK_ENGLISH_AVG"].",";
        $contents .= $Row["TEST_VALUATION_AVG"].",";
        $contents .= $Row["TEST_VALUATION_PERCENT_SCORE"].",";
        $contents .= $Row["MOCK_TOTAL_AVG"].",";
        $contents .= $Row["MOCK_TOTAL_PERCENT_SCORE"].",";
        $contents .= ",";
        $contents .= ",";
        $contents .= $Row["TOTAL_SCORE"].","; //学内成績 総合成績
        $contents .= "\n"; //改行

        //学年平均  評定
        $contents .= "学年平均,";
        $contents .= ",";
        $contents .= ",";
        $avgArr = array();
        $query = knjh442aQuery::getAvgRecordRank($this, "GRADE", $this->field["GRADE"]);
        $result = $db->query($query);
        while ($Row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $avgArr[$Row["SUBCLASSCD"]] = $Row["SCORE_AVG"];
        }
        $result->free();
        foreach ($subclassArr as $key => $subclassCd) {
            $contents .= $avgArr[$subclassCd].",";
        }
        $contents .= ",";

        //学年平均  総合成績
        $query = knjh442aQuery::getAvgAftSchregRecommendationRank($this, "GRADE", $this->field["GRADE"]);
        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $contents .= $Row["MOCK_NATIONAL_LANGUAGE_AVG"].",";
        $contents .= $Row["MOCK_MATH_AVG"].",";
        $contents .= $Row["MOCK_ENGLISH_AVG"].",";
        $contents .= $Row["TEST_VALUATION_AVG"].",";
        $contents .= $Row["TEST_VALUATION_PERCENT_SCORE"].",";
        $contents .= $Row["MOCK_TOTAL_AVG"].",";
        $contents .= $Row["MOCK_TOTAL_PERCENT_SCORE"].",";
        $contents .= ",";
        $contents .= ",";
        $contents .= $Row["TOTAL_SCORE"].","; //学内成績 総合成績
        $contents .= "\n"; //改行

        //DB切断
        Query::dbCheckIn($db);

        return $contents;
    }
}
