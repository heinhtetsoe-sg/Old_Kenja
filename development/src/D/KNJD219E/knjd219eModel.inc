<?php

require_once('for_php7.php');

class knjd219eModel extends Model {
    var $field       = array();
    var $low_stock   = array();
    var $high_stock  = array();
    var $cmd;
    var $sepa;
    var $level;

    function knjd219eModel() {}

    function init() {
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

        if ($this->cmd == 'update' || $this->cmd == 'sim') {
            for ($i = 1; $i <= $this->field["ASSESSLEVELCNT"]; $i++) {
                $this->field["ASSESSLOW" .$i] = VARS::post("ASSESSLOW" .$i);
                $this->field["ASSESSHIGH".$i] = VARS::post("ASSESSHIGH".$i);
                $this->field["PERCENT"   .$i] = VARS::post("PERCENT".$i);
            }
        }
    }

    /********************/
    /* 入力項目チェック */
    /********************/
    function validateInput() {
        return true;
    }

    /********************/
    /* シュミレーション */
    /********************/
    function &getSimModel() {
        //入力チェック
        if ($this->validateInput()) {
            $db = Query::dbCheckOut();

            $minLow = $this->field["ASSESSLOW1"];
            $maxHigh = $this->field["ASSESSHIGH".$this->field["ASSESSLEVELCNT"]];
            $query = knjd219eQuery::getCountPercent($this, $minLow, $maxHigh);
            $cntALL = $db->getOne($query);

            for ($i = 1; $i <= $this->field["ASSESSLEVELCNT"]; $i++) {
                if ($cntALL == 0) {
                    $percent = 0;
                    $this->field["PERCENT".$i] = sprintf("%.1f", $percent);
                    continue;
                }
                $assessLow = $this->field["ASSESSLOW" .$i];
                if ($i == $this->field["ASSESSLEVELCNT"]) {
                    $assessHigh = $this->field["ASSESSHIGH".$i];
                } else {
                    $assessHigh = ($this->field["ASSESSLOW".($i + 1)] - 1);
                }
                $query = knjd219eQuery::getCountPercent($this, $assessLow, $assessHigh);
                $cnt = $db->getOne($query);
//echo "cntALL=" .$cntALL .", cnt=" .$cnt ."<BR>";
                $percent = round(($cnt / $cntALL * 100) * 10) / 10;
                $this->field["PERCENT".$i] = sprintf("%.1f", $percent);
            }

            Query::dbCheckIn($db);

            $this->setMessage("MSG201","確定ボタンを押下しないとデータは保存されません。");
        }
        return false;
    }

    /****************/
    /* アップデート */
    /****************/
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            knjd219eQuery::insAssessLevel($this);
            //成績更新処理はしない。必要になるかもしれないので記述は残して置く。
            //knjd219eQuery::insRecordScore($this);
            $this->setMessage("MSG201");
        }
        return false;
    }

    /**********/
    /* コピー */
    /**********/
    function &getCopyModel()
    {
        $result = knjd219eQuery::getCopyQuery($this);
        $this->setMessage("MSG201");
        return true;
    }

    //変数をクリアします
    function clean() {}
}
?>
