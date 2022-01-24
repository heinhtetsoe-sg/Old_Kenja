<?php

require_once('for_php7.php');

class knjd219cModel extends Model
{
    public $field       = array();
    public $low_stock   = array();
    public $high_stock  = array();
    public $cmd;
    public $sepa;
    public $level;

    public function knjd219cModel()
    {
    }

    public function init()
    {
        $this->cmd  = VARS::request("cmd");
        //プロパティファイル
        $this->getPropertiesAll();
        $this->field = array("PRE_SEMESTER"     =>  VARS::post("PRE_SEMESTER"), //学期(参照)
                             "PRE_TESTKINDCD"   =>  "9900",                     //成績種別(参照)
                             "SEMESTER"         =>  VARS::request("SEMESTER"),  //学期(対象)
                             "TESTKINDCD"       =>  "9900",                     //成績種別(対象)
                             "DIV"              =>  VARS::request("DIV"),       //区分
                             "GRADE"            =>  VARS::request("GRADE"),     //学年
                             "HR_CLASS"         =>  VARS::post("HR_CLASS"),     //クラス
                             "COURSE"           =>  VARS::post("COURSE"),       //課程学科コース
                             "GROUP_CD"         =>  VARS::post("GROUP_CD"),     //コースグループ
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),   //科目
                             "ASSESSLEVELCNT"   =>  5                           //評定段階数
                             );

        if ($this->cmd == 'update' || $this->cmd == 'sim' || $this->cmd == 'standard') {
            for ($i = 1; $i <= $this->field["ASSESSLEVELCNT"]; $i++) {
                $this->field["ASSESSLOW" .$i] = VARS::post("ASSESSLOW" .$i);
                $this->field["ASSESSHIGH".$i] = VARS::post("ASSESSHIGH".$i);
                $this->field["PERCENT"   .$i] = VARS::post("PERCENT".$i);
                $this->field["PERCENT_CNT"   .$i] = $this->field2["PERCENT_CNT".$i];
                $this->field["PERCENT_ALLCNT"   .$i] = $this->field2["PERCENT_ALLCNT".$i];
                $this->field["STANDARD_ASSESSLOW" .$i] = VARS::post("STANDARD_ASSESSLOW" .$i);
            }
        }
    }

    /********************/
    /* 入力項目チェック */
    /********************/
    public function validateInput()
    {
        return true;
    }

    /********************/
    /* 標準の下限値計算 */
    /********************/
    public function &getStandardModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $db = Query::dbCheckOut();

            //初期化
            for ($i = 1; $i <= $this->field["ASSESSLEVELCNT"]; $i++) {
                $this->field["STANDARD_ASSESSLOW" .$i] = "";
            }

            //成績を降順にして取得
            $cntALL = 0;            //母集団の人数
            $scoreArray = array();  //成績(降順)の配列
            $query = knjd219cQuery::getCountPercent($this, "DUMMY", "DUMMY", "standard");
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $cntALL++; //母集団の人数をカウント
                $scoreArray[$row["GRAD_SCORE"]] = $cntALL; //成績を配列に保持
            }
            $result->free();

            //標準の下限値(上位20%,60%,下位10%)を人数割合から計算(降順)
            $score10 = "";
            foreach ($scoreArray as $score => $cnt) {
                $percent = round(($cnt / $cntALL * 100) * 10) / 10;
                $percent = sprintf("%.1f", $percent); //人数割合(上位)
                //評定5の下限値
                if ($percent <= 20) {
                    $this->field["STANDARD_ASSESSLOW5"] = $score; //評定5の下限値 上位20%以内の最小値
                }
                //評定4の下限値
                if ($percent <= 60) {
                    $this->field["STANDARD_ASSESSLOW4"] = $score; //評定4の下限値 上位60%以内の最小値
                }
                //評定2,3の下限値
                if ($percent >  90 && !strlen($score10)) {
                    $score10 = $score; //下位10%未満を満たす最大値
                    if ($score10 >= 40) {
                        $this->field["STANDARD_ASSESSLOW3"] = 40; //評定3の下限値 40
                        $this->field["STANDARD_ASSESSLOW2"] = 40; //評定2の下限値 40
                    } elseif ($score10 >= 30 && $score10 <= 39) {
                        $this->field["STANDARD_ASSESSLOW3"] = $score10; //評定3の下限値 下位10%未満を満たす最大値
                        $this->field["STANDARD_ASSESSLOW2"] = $score10; //評定2の下限値 下位10%未満を満たす最大値
                    } elseif ($score10 <= 29) {
                        $this->field["STANDARD_ASSESSLOW3"] = 30;       //評定3の下限値 30
                        $this->field["STANDARD_ASSESSLOW2"] = $score10; //評定2の下限値 下位10%未満を満たす最大値
                    }
                }
                //評定1の下限値
                $this->field["STANDARD_ASSESSLOW1"] = 0; //評定1の下限値 0
//echo "cnt=" .$cnt .", cntALL=" .$cntALL .", score=" .$score .", percent=" .$percent ."<BR>";
            }

            Query::dbCheckIn($db);

            $this->setMessage("MSG201", "シュミレーションおよび確定ボタンを押下しないとデータは保存されません。");
        }
        return false;
    }

    /********************/
    /* シュミレーション */
    /********************/
    public function &getSimModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $db = Query::dbCheckOut();

            $minLow = $this->field["ASSESSLOW1"];
            $maxHigh = $this->field["ASSESSHIGH".$this->field["ASSESSLEVELCNT"]];
            $query = knjd219cQuery::getCountPercent($this, $minLow, $maxHigh);
            $cntALL = $db->getOne($query);

            for ($i = 1; $i <= $this->field["ASSESSLEVELCNT"]; $i++) {
                if ($cntALL == 0) {
                    $percent = 0;
                    $this->field["PERCENT".$i] = sprintf("%.1f", $percent);
                    $this->field["PERCENT_ALLCNT".$i] = $cntALL;
                    $this->field["PERCENT_CNT".$i] = $cnt;
                    $this->field2["PERCENT_ALLCNT".$i] = $cntALL;
                    $this->field2["PERCENT_CNT".$i] = $cnt;
                    continue;
                }
                $assessLow = $this->field["ASSESSLOW" .$i];
                if ($i == $this->field["ASSESSLEVELCNT"]) {
                    $assessHigh = $this->field["ASSESSHIGH".$i];
                } else {
                    $assessHigh = ($this->field["ASSESSLOW".($i + 1)] - 1);
                }
                $query = knjd219cQuery::getCountPercent($this, $assessLow, $assessHigh);
                $cnt = $db->getOne($query);
//echo "cntALL=" .$cntALL .", cnt=" .$cnt ."<BR>";
                $percent = round(($cnt / $cntALL * 100) * 10) / 10;
                $this->field["PERCENT".$i] = sprintf("%.1f", $percent);
                $this->field["PERCENT_ALLCNT".$i] = $cntALL;
                $this->field["PERCENT_CNT".$i] = $cnt;
                $this->field2["PERCENT_ALLCNT".$i] = $cntALL;
                $this->field2["PERCENT_CNT".$i] = $cnt;
            }

            Query::dbCheckIn($db);

            $this->setMessage("MSG201", "確定ボタンを押下しないとデータは保存されません。");
        }
        return false;
    }

    /****************/
    /* アップデート */
    /****************/
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            knjd219cQuery::insAssessLevel($this);
            knjd219cQuery::insRecordScore($this);
            $this->setMessage("MSG201");
        }
        return false;
    }

    /**********/
    /* コピー */
    /**********/
    public function &getCopyModel()
    {
        $result = knjd219cQuery::getCopyQuery($this);
        $this->setMessage("MSG201");
        return true;
    }

    //変数をクリアします
    public function clean()
    {
    }
}
?>
