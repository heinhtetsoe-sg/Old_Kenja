<?php

require_once('for_php7.php');

class knjd141cModel extends Model
{
    public $grade;
    public $school_kind;
    public $exam;
    public $seme;
    public $kind;
    public $item;
    public $sdiv;
    public $seme_sch;
    public $cmd;
    public $control;
    public $chairdate;
    public $subclasscd;
    public $electdiv;
    public $tableStr;
    public $schoolName;
    public $assess_chk;
    public $calcDeviationFlg;

    public function knjd141cModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->cmd      = VARS::request("cmd");
        $this->course_major = VARS::post("COURSE_MAJOR"); //課程学科コンボ
        $this->grade    = VARS::post("GRADE");
        $this->school_kind = VARS::post("SCHOOL_KIND"); //学校種別
        $this->exam     = VARS::post("EXAM");
        $test = explode("-", VARS::post("EXAM"));
        $this->seme     = VARS::post("SEMESTER");
        $this->kind     = $test[0];
        $this->item     = $test[1];
        $this->sdiv     = $test[2];
        $this->seme_sch = ($this->seme != "9") ? $this->seme : CTRL_SEMESTER;
        $this->chairdate = VARS::post("CHAIRDATE");
        $this->subclasscd = VARS::post("SUBCLASSCD");
        $this->assess_chk = VARS::post("ASSESS_CHK");
        //メニュー起動時
        if (VARS::get("PROGRAMID")) {
            $this->electdiv = "1"; //初期値
        } else {
            $this->electdiv = VARS::post("ELECTDIV");
        }
        //更新テーブル _dat:record_rank_dat _v_dat:record_rank_v_dat _sdiv_dat:record_rank_sdiv_dat
        $this->tableStr = "_sdiv_dat";
        //echo "更新テーブル：record_rank" .$this->tableStr;
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        //学校名取得
        $db = Query::dbCheckOut();
        $query = knjd141cQuery::getNameMstZ010();
        $this->schoolName = $db->getOne($query);
        //合算科目の母集団から除くフラグ 1:合併元除く 2:合併先除く
        if ($this->schoolName == "rakunan" || $this->schoolName == "tosa") {
            $this->d070Namespare1 = "";
        } else {
            $dx70 = $db->getOne(knjd141cQuery::getNameMstD070($this, "D{$this->school_kind}70"));
            $d070 = $db->getOne(knjd141cQuery::getNameMstD070($this, "D070"));
            $this->d070Namespare1 = strlen($dx70) ? $dx70 : $d070;
        }
        if ($this->Properties["useRank_record_slump_sdiv_dat"] == '1') {
            $this->sidouInputInf = $db->getOne(knjd141cQuery::getSidouInputInf($this));
        }

        //平均点の小数点以下の持ち方(基本：小数点第5位)
        //SQL例・・・DECIMAL(ROUND(AVG(FLOAT(SCORE))*100000,0)/100000,9,5) AS AVG
        //SQL例・・・DECIMAL(ROUND(AVG(FLOAT(SCORE))*{$this->AvgDecimalPoint["waru"]},0)/{$this->AvgDecimalPoint["waru"]},{$this->AvgDecimalPoint["allketa"]},{$this->AvgDecimalPoint["keta"]}) AS AVG
        $this->AvgDecimalPoint = array();
        $this->AvgDecimalPoint["keta"] = 5;
        if (strlen($this->Properties["knjd141c_AvgDecimalPoint"])) {
            $this->AvgDecimalPoint["keta"] = $this->Properties["knjd141c_AvgDecimalPoint"];
        }
        $this->AvgDecimalPoint["allketa"] = $this->AvgDecimalPoint["keta"] + 4;
        $this->AvgDecimalPoint["waru"] = 1;
        for ($i = 0; $i < $this->AvgDecimalPoint["keta"]; $i++) {
            $this->AvgDecimalPoint["waru"] = $this->AvgDecimalPoint["waru"] * 10;
        }

        Query::dbCheckIn($db);
    }

    public function &getMainModel()
    {
    }

    //事前処理チェック
    public function validateInput()
    {
        //存在チェック（在籍データ）
        $cnt_sch = knjd141cQuery::checktoSchregno($this);
        if (0 < $cnt_sch) {
        } else {
            $this->setWarning("MSG303", "\\n(学籍在籍データ)\\n学年または組またはコースが設定されているか確認して下さい。");
            return false;
        }
        //存在チェック（成績データ）
        $cnt_rec = knjd141cQuery::checktoRecordScore($this);
        if (0 < $cnt_rec) {
        } else {
            $this->setWarning("MSG303", "\\n(成績データ)");
            return false;
        }

        return true;
    }

    /**
     * 実行モデルを返します
    */
    public function &getExecModel()
    {
        //事前処理チェック
        if ($this->validateInput()) {
            //満点で偏差値計算を判別（1人でも科目の満点が複数ある場合、合算科目の偏差値は、各科目の偏差値の平均を算出する）
            $rtnCnt = knjd141cQuery::getPerfectCnt($this);
            $this->calcDeviationFlg = (0 < $rtnCnt || $this->Properties["knjd141c_deviationTotalRank"] == '1') ? "1" : "";
            $this->narakenKekkaOverSubclass = knjd141cQuery::getNarakenKekkaOverSchregnoSubclass($this);
            knjd141cQuery::executeRireki($this);
            if (knjd141cQuery::executeQuery($this)) {
                $this->setMessage("MSG202");
                //評定マスタ作成　成城要望
                if ($this->Properties["knjd141c_useRelativeAssessCheckbox"] == '1' && $this->assess_chk == "1") {
                    knjd141cQuery::executeAssess($this);
                }
                //教科別序列　玉川聖学院から
                //処理科目(全て)、考査種別(X-99-00-09)を指定した時のみ実行
                if ($this->subclasscd == '000000' && $this->kind == '99' && $this->item == '00' && $this->sdiv == '09') {
                    $db = Query::dbCheckOut();
                    $rnkCnt = $db->getOne(knjd141cQuery::existsTable("RECORD_RANK_CLASS_SDIV_DAT"));
                    $avgCnt = $db->getOne(knjd141cQuery::existsTable("RECORD_AVERAGE_CLASS_SDIV_DAT"));
                    Query::dbCheckIn($db);
                    if (0 < $rnkCnt && 0 < $avgCnt) {
                        knjd141cQuery::updRecordRankClassDat($this);
                        knjd141cQuery::updRecordAvgClassDat($this);
                    }
                }
                //基準点設定　桜花学園から
                //処理科目(全て、各科目)、考査種別は評価(X-XX-XX-08)を指定した時のみ実行
                if ($this->subclasscd != '999999' && $this->sdiv == '08') {
                    $db = Query::dbCheckOut();
                    $rtnCnt = $db->getOne(knjd141cQuery::existsTable("STANDARD_SCORE_DAT"));
                    Query::dbCheckIn($db);
                    if (0 < $rtnCnt) {
                        knjd141cQuery::updStandardScoreDat($this);
                    }
                }
                //換算点算出処理　青山学院高等部
                //考査種別(X-99-00-08)を指定した時のみ実行
                if ($this->kind == '99' && $this->item == '00' && $this->sdiv == '08') {
                    $db = Query::dbCheckOut();
                    $tblCnt1 = $db->getOne(knjd141cQuery::existsTable("RECORD_SCORE_KANSAN_DAT"));
                    $tblCnt2 = $db->getOne(knjd141cQuery::existsTable("RECORD_SCORE_KANSAN_TOTAL_DAT"));
                    Query::dbCheckIn($db);
                    if (0 < $tblCnt1 && 0 < $tblCnt2) {
                        knjd141cQuery::updRecordScoreKansan($this);
                    }
                }
            }
        }
    }
}
