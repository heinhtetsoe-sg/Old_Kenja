<?php

/**
 * 定数定義
*/

class knjh442cModel extends Model
{
    public $field      = array();
    public $cmd;
    public $grade;
    public $control;   //コントロールマスタ
    public $categorySelected;
    public $percentage;
    public $percentage2;
        
    /**
     * メンバ変数の初期化
     */
    public function knjh442cModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "TITLEDIV"         =>  VARS::post("TITLEDIV"),         //指定校推薦、日大付属特別選抜
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "S_MONTH"          =>  VARS::post("S_MONTH"),
                             "E_MONTH"          =>  VARS::post("E_MONTH"),
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
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

        // //AFT_RECOMMENDATION_RANK_HEAD_DAT の取得
        // $db = Query::dbCheckOut();
        // $query = knjh442cQuery::getAftRecommendationRankHead($this);
        // $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        // $this->percentage = $row["PERCENTAGE"];
        // $this->percentage2 = 100 - $this->percentage;

        //DB切断
        Query::dbCheckIn($db);
        
        //初期化
        $contents = "";

        $data = false;
        foreach ($this->categorySelected as $key => $schregno) {
            $contents .= $this->setContents($schregno, $data);
            $contents .= "\n"; //改行
            $contents .= "\n"; //改行
            $contents .= "\n"; //改行
        }

        //CSV出力
        if ($data) {
            $title = $this->field["YEAR"]."年度 成績状況表";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    //データ出力
    public function setContents($schregno, &$data)
    {
        //DB接続
        $db = Query::dbCheckOut();

        //初期化
        $contents = "";

        //生徒情報を保持
        $query = knjh442cQuery::getSchregInfo($this, $schregno);
        $schregInfo = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //評定を保持 (1)
        $query = knjh442cQuery::getReocrdRankSdiv($this, $schregInfo["SCHREGNO"]);
        $result = $db->query($query);
        $recordRankSdivArr = array();
        while ($Row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $key = $Row["SUBCLASSCD"] == "999999" ? $Row["SUBCLASSCD"] : $Row["CLASSCD"]."-".$Row["SCHOOL_KIND"]."-".$Row["CURRICULUM_CD"]."-".$Row["SUBCLASSCD"];
            $recordRankSdivArr[$key] = $Row["SCORE"];
        }

        //総合成績を保持 (2, 4)
        $query = knjh442cQuery::getAftSchregRecommendationRank($this, $schregInfo["SCHREGNO"]);
        $aftSchregRecommendationRank = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //所見を保持 (ヘッダ, 5, 6)
        $query = knjh442cQuery::getMockTrainremark($this, $schregno);
        $mockInfo = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //1行目
        $query = knjh442cQuery::getCourseName($this, $schregno);
        $course = $db->getOne($query);
        $contents .= $course.","; //コース名称
        if ($modek->field["TITLEDIV"] == "1") {
            $contents .= "指定校推薦,";
            $contents .= $mockInfo["HOPE_COLLEGE_NAME1"].$mockInfo["HOPE_COURSE_NAME1"].",";
        } else {
            $contents .= "日大付属特別選抜,";
            $contents .= $mockInfo["HOPE_COLLEGE_NAME2"].$mockInfo["HOPE_COURSE_NAME2"].",";
        }
        $contents .= $schregInfo["GRADE_NAME2"]."　".$schregInfo["HR_NAMEABBV"]."組　".$schregInfo["ATTENDNO"]."番,"; //年組番
        $contents .= $schregInfo["NAME"].",";
        $contents .= "\n"; //改行

        //1.科目評定 ヘッダ
        $contents .= "1.科目評定\n";
        $classCdArr = ["01","02","03","04","05","06"];
        $classNameArr = ["国語","地理","公民","数学","理科","英語"];
        foreach ($classNameArr as $key => $className) {
            $contents .= $classNameArr[$key].",";
            $contents .= ",";
        }
        $contents .= "\n"; //改行

        //1.科目評定 評定平均
        foreach ($classCdArr as $key => $classCd) {
            $query = knjh442cQuery::getRecordRankAvg($this, $schregno, $classCd);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $contents .= round($row["SCORE_AVG"], 1).","; //評定平均
            $contents .= ",";
        }
        $contents .= "\n"; //改行

        //1.科目評定 明細 取得
        $avgArr = array();
        foreach ($classCdArr as $key => $classCd) {
            $query = knjh442cQuery::getSubclassName($this, $schregno, $classCd);
            $result = $db->query($query);
            $arr = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $subclasscd = $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"];
                $arr[] = $row["CLASSCD"]."-".$row["SUBCLASSNAME"].",".round($recordRankSdivArr[$subclasscd], 1); //科目名称 + 評定
            }
            $avgArr[] = $arr;
        }

        //1.科目評定 明細 出力
        for ($line = 0; $line <= 17; $line++) {
            $flg = false;
            foreach ($avgArr as $key => $arr) {
                if ($arr[$line]) {
                    $contents .= $arr[$line]; //科目名称 + 評定
                    $contents .= ",";
                    $flg = true;
                } else {
                    $contents .= ",";
                    $contents .= ",";
                }
            }
            $contents .= "\n"; //改行
            if (!$flg) {
                break;
            }
        }

        //2.実力テスト
        $contents .= "2.実力テスト\n";
        $contents .= "国語,";
        $contents .= "数学,";
        $contents .= "英語,";
        $contents .= "実力平均100%,";
        $contents .= "\n"; //改行
        $contents .= round($aftSchregRecommendationRank["MOCK_NATIONAL_LANGUAGE_AVG"], 2).",";
        $contents .= round($aftSchregRecommendationRank["MOCK_MATH_AVG"], 2).",";
        $contents .= round($aftSchregRecommendationRank["MOCK_ENGLISH_AVG"], 2).",";
        $contents .= round($aftSchregRecommendationRank["MOCK_TOTAL_AVG"], 2).",";
        $contents .= "\n"; //改行
        $contents .= "\n"; //改行

        //3.出欠状況
        $yearLine = 1;
        $contents .= "3.出欠状況\n";
        $contents .= ",";
        $contents .= "欠席,";
        $contents .= "遅刻,";
        $contents .= "早退,";
        $contents .= "\n"; //改行
        $query = knjh442cQuery::getAttend($this, $schregno);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $contents .= $yearLine."年,";
            $contents .= $row["SICK"].",";
            $contents .= $row["LATE"].",";
            $contents .= $row["EARLY"].",";
            $contents .= "\n"; //改行
            $yearLine++;
        }
        $contents .= "\n"; //改行

        //4.総合成績
        $contents .= "4.総合成績\n";
        $contents .= "評定平均,";
        $contents .= round($aftSchregRecommendationRank["TEST_VALUATION_AVG"], 2).",";
        $contents .= "\n"; //改行
        $contents .= "評定 80%,";
        $contents .= round($aftSchregRecommendationRank["TEST_VALUATION_PERCENT_SCORE"], 2).",";
        $contents .= "\n"; //改行
        $contents .= "実力 20%,";
        $contents .= round($aftSchregRecommendationRank["MOCK_TOTAL_PERCENT_SCORE"], 2).",";
        $contents .= "\n"; //改行
        $contents .= "総合成績,";
        $contents .= round($aftSchregRecommendationRank["TOTAL_SCORE"], 2).",";
        $contents .= "\n"; //改行
        $contents .= "\n"; //改行

        //5.行動の記録など（クラブ活動・生徒会活動・表彰・資格など）
        $contents .= "5.行動の記録など（クラブ活動・生徒会活動・表彰・資格など）\n";
        $contents .= "（１年）,";
        $contents .= $mockInfo["GRADE1_REMARK"].",";
        $contents .= "\n"; //改行
        $contents .= "（２年）,";
        $contents .= $mockInfo["GRADE2_REMARK"].",";
        $contents .= "\n"; //改行
        $contents .= "（３年）,";
        $contents .= $mockInfo["GRADE3_REMARK"].",";
        $contents .= "\n"; //改行
        $contents .= "\n"; //改行

        //6.条件
        $contents .= "6.条件\n";
        $contents .= $mockInfo["CONDITION"].",";
        $contents .= "\n"; //改行
        $contents .= "\n"; //改行

        $data = true;

        //DB切断
        Query::dbCheckIn($db);

        return $contents;
    }
}
