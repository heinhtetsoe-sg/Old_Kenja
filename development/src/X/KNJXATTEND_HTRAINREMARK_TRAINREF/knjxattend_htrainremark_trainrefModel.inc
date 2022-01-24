<?php

require_once('for_php7.php');

class knjxattend_htrainremark_trainrefModel extends Model
{
    public $cmd;
    public $schregno;          //学籍番号
    public $year;              //年度

    public function knjxattend_htrainremark_trainrefModel()
    {
    }

    public function init()
    {
        $this->schregno         = VARS::request("SCHREGNO");
        $this->year             = VARS::post("YEAR");
        $this->output_height    = VARS::request("OUTPUT_HEIGHT") ? VARS::request("OUTPUT_HEIGHT") : "75";
        $this->output_width     = VARS::request("OUTPUT_WIDTH") ? VARS::request("OUTPUT_WIDTH") + 80 : "200";
        $this->moji = 26;
        $this->gyou = 4;

        $db = Query::dbCheckOut();
        $z010 = $db->getOne(knjxattend_htrainremark_trainrefQuery::getZ010());
        Query::dbCheckIn($db);
        if ($z010 == "shimaneken") {
            // 項目名
            $this->itemname = array(
                "TRAIN_REF1" => "(1)各教科・科目等の学習に関する所見",
                "TRAIN_REF2" => "(2)行動に関する所見・特技等",
                "TRAIN_REF3" => "(3)進路指導に関する事項",
                "TRAIN_REF4" => "(4)取得資格、検定等",
                "TRAIN_REF5" => "(5)部活動、ボランティア活動等",
                "TRAIN_REF6" => "(6)その他"
            );
        } else {
            // 項目名
            $this->itemname = array(
                "TRAIN_REF1" => "(1)学習における特徴等",
                "TRAIN_REF2" => "(2)行動の特徴，特技等",
                "TRAIN_REF3" => "(3)部活動，ボランティア活動，留学・海外経験等",
                "TRAIN_REF4" => "(4)取得資格，検定等",
                "TRAIN_REF5" => "(5)表彰・顕彰等の記録",
                "TRAIN_REF6" => "(6)その他"
            );
        }
    }
}
