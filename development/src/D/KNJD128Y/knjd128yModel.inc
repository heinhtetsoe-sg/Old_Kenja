<?php

require_once('for_php7.php');

class knjd128yModel extends Model
{
    public $field = array();
    public $data  = array();
    public $testcdArray  = array();
    public $cmd;
    public $usePerfect;
    public $chikokuHyoujiFlg;
    public $useSlumpHyouka;
    public $useCsv;
    public $subclassAll;
    public $fieldsCalc = array();
    public $testcdMotoArray = array();
    public $schregno;
    public $attendno;
    public $name_show;
    public $z010name1;
    public $lastColumn;//CSVの最後の列の値

    public function knjd128yModel()
    {
    }

    public function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            $this->data = array();
            $this->testcdArray = array();
            $this->testcdMotoArray = array();
            common::GetControlMaster_Fnc($this->control);
        }
        $this->field["MOVE_ENTER"]  = VARS::post("MOVE_ENTER"); //エンター押下時の移動方向
        $this->cmd     = VARS::request("cmd");
        $this->field["COURSE_MAJOR"] = VARS::request("COURSE_MAJOR"); //課程学科コンボ
        //科目コード
        $this->field["SUBCLASSCD"] = VARS::request("SUBCLASSCD");
        if ($this->cmd == "update" || $this->cmd == "calc") {
            $this->field["SUBCLASSCD"] = VARS::post("H_SUBCLASSCD");
        }

        list($classcd, $this->school_kind, $curriculumcd, $subclasscd) = explode("-", $this->field["SUBCLASSCD"]);

        //講座コード
        if ($this->cmd!="subclasscd"&&VARS::request("CHAIRCD")) {
            $this->field["CHAIRCD"] = VARS::request("CHAIRCD");
        } elseif ($this->cmd == "update" || $this->cmd == "calc") {
            $this->field["CHAIRCD"] = VARS::post("H_CHAIRCD");
        } else {
            $this->field["CHAIRCD"] = "";
        }
        $this->field["SIDOU_CHK"] = VARS::post("SIDOU_CHK");
        $this->field["KEEKA_OVER"] = VARS::post("KEEKA_OVER");
        $this->field["COUNTER"]  = VARS::post("COUNTER"); //行数

        //出欠情報
        $this->field["APPOINTED_DAY"]   = VARS::post("APPOINTED_DAY");  //締め日
        $this->field["MONTH_SEMESTER"]  = VARS::post("MONTH_SEMESTER"); //対象月（学期）
        if ($this->cmd == "update" || $this->cmd == "calc") {
            $this->field["MONTH_SEMESTER"]  = VARS::post("H_MONTH_SEMESTER"); //対象月（学期）
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        //初期値の設定
        $this->usePerfect = (strlen($this->Properties["usePerfect"])) ? $this->Properties["usePerfect"] : false;
        $this->chikokuHyoujiFlg = (strlen($this->Properties["chikokuHyoujiFlg"])) ? $this->Properties["chikokuHyoujiFlg"] : 0;
        //「評価にリンクを付けるか？」のフラグを取得
        $rtnRow = knjd128yQuery::getNameMstD048();
        $this->useSlumpHyouka = $rtnRow["NAMESPARE1"] == "1" ? true : false; // 1:評価にリンクを付ける
        //「ＣＳＶは、非表示とする？」のフラグを取得
        $rtnRow = knjd128yQuery::getNameMstD058();
        $this->useCsv = $rtnRow["NAMESPARE1"] == "1" ? false : true; // 1:非表示
        //学校名取得
        $this->z010name1 = knjd128yQuery::getNameMstZ010();
        //各種設定を取得・・・洛南要望
        $rtnRow = knjd128yQuery::getNameMstSettei("D067", "01");
        $this->noUseSouGaku     = $rtnRow["NAMESPARE1"];//1:総学非表示（９０未満を表示とする）
        $this->noUseBtnAttend   = $rtnRow["NAMESPARE2"];//1:出欠入力ボタン非表示
        //宮城県の場合、初期値のボタン名「小表」を「小表(確認用)」に変更
        $wordBtnPrintDef = "小 表";
        if ($this->z010name1 == "miyagiken") {
            $wordBtnPrintDef = "小表(確認用)";
        }
        $this->wordBtnPrint     = $rtnRow["ABBV1"] != "" ? $rtnRow["ABBV1"] : $wordBtnPrintDef;//小表ボタン文言

        //算出ボタン押し下げ時のみ保持（それ以外は初期化）
        $this->fieldsCalc = array();
        $this->field["SUB_D065"]  = VARS::post("SUB_D065"); //1:D065登録科目
        if ($this->cmd == "calc") {
            //学期評価・学年評価を算出
            //評価の算出方法(1:四捨五入 2:切り上げ 3:切り捨て)
            $rtnRow = knjd128yQuery::getCalcMethod();
            $this->getCalcModel($rtnRow["SCHOOL_REMARK1"]);
        }

        if (VARS::get("SCHREGNO")) {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("ATTENDNO")) {
            $this->attendno = VARS::get("ATTENDNO");
        }
        if (VARS::get("NAME_SHOW")) {
            $this->name_show = VARS::get("NAME_SHOW");
        }

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        return true;
    }

    public function &getUpdateModel()
    {
        //初期化
        $this->fields = array();
        $this->testInsArray = array();
        for ($i = 0; $i < $this->field["COUNTER"]; $i++) {
            //学籍番号を更新時に取得
            $this->fields["SCHREGNO"][] = VARS::post("SCHREGNO"."-".$i);
            foreach ($this->testcdArray as $key => $codeArray) {
                $testcd = $codeArray["TESTCD"];

                //欠席者で見込点があれば、更新不可
                if ($this->Properties["useMikomiFlg"] == '1') {
                    $this->fields["MIKOMI".$testcd][] = VARS::post("SCORE".$testcd."_MIKOMI"."-".$i);
                }

                $score = "SCORE".$testcd."-".$i;
                $this->fields["SCORE".$testcd][] = VARS::post($score);
                $this->field["CHK_COMP".$testcd] = VARS::post("CHK_COMP".$testcd);

                //成績が１つでも入力された考査コードを配列に保管
                if (strlen(VARS::post($score))) {
                    $this->testInsArray[$testcd] = $testcd;
                }
            }

            //出欠情報
            $this->fields["LESSON"][]   = VARS::post("LESSON"."-".$i);
            $this->fields["NONOTICE"][] = VARS::post("NONOTICE"."-".$i);
            $this->fields["LATE"][]     = VARS::post("LATE"."-".$i);
            $this->fields["EARLY"][]    = VARS::post("EARLY"."-".$i);
        }

        if ($this->validateInput()) {
            knjd128yQuery::update($this);
            knjd128yQuery::updateRecordChkfin($this);
            if (strlen($this->field["MONTH_SEMESTER"]) && strlen($this->field["APPOINTED_DAY"])) {
                knjd128yQuery::updateAttendSubclass($this);
            }
            $this->setMessage("MSG201");
        }
    }

    public function &getCalcModel($calcMethod)
    {
        //初期化
        $this->fieldsCalc = array();
        //画面上から取得
        for ($i = 0; $i < $this->field["COUNTER"]; $i++) {
            foreach ($this->testcdArray as $key => $codeArray) {
                $name = "SCORE".$codeArray["TESTCD"]."-".$i;
                $this->fieldsCalc[$name] = VARS::post($name);
            }
        }
        //学期評価・学年評価を算出
        for ($i = 0; $i < $this->field["COUNTER"]; $i++) {
            //異動日の翌日以降は、転学、退学者は、入力不可にする。(算出用)
            $tentaigakuFlg = VARS::post("TENTAIGAKU_FLG"."-".$i);
            if ($tentaigakuFlg && AUTHORITY != DEF_UPDATABLE) {
                continue;
            }
            foreach ($this->testcdMotoArray as $testcd => $array) {
                $abcentFlg = false;
                $term_data = array();
                $perfect_data = array();
                foreach ($array as $key => $codeArray) {
                    $nameMoto = "SCORE".$codeArray["MOTO_TESTCD"]."-".$i;
                    $score = $this->fieldsCalc[$nameMoto];
                    $nameAss = "SCORE".$codeArray["MOTO_TESTCD"]."_ASSESSLOW2"."-".$i;
                    $assesslow2 = (strlen($this->field["SIDOU_CHK"])) ? VARS::post($nameAss) : "";
                    //満点
                    $perfect = VARS::post("SCORE".$codeArray["MOTO_TESTCD"]."_PERFECT"."-".$i);
                    //見込点
                    $mikomi = ($this->Properties["useMikomiFlg"] == '1') ? VARS::post("SCORE".$codeArray["MOTO_TESTCD"]."_MIKOMI"."-".$i) : "";
                    //数値セット
                    if (is_numeric($mikomi) && $score == "*") { //欠席者で見込点があれば、見込点を含めて算出
                        $term_data[] = $mikomi;
                        $perfect_data[] = $perfect;
                    } elseif (is_numeric($assesslow2) && $score < $assesslow2) {
                        $term_data[] = $assesslow2;
                        $perfect_data[] = $perfect;
                    } elseif (is_numeric($score)) {
                        $term_data[] = $score;
                        $perfect_data[] = $perfect;
                    }
                    //欠席フラグ
                    if ($score == "*" && !is_numeric($assesslow2) && !is_numeric($mikomi)) {
                        $abcentFlg = true;
                    }
                }
                //平均
                $name = "SCORE".$testcd."-".$i;
                if (get_count($term_data)) {
                    //総合的な学習の時間(D065登録科目)の年間評価算出・・・埼玉栄要望
                    //各学期評価：A = 11、B = 22、C = 33
                    //各学期評価を合計し、１～３学期の場合、44以下がA、77以下がB、それより上がC
                    //各学期評価を合計し、１・２学期の場合、33以下がA、55以下がB、それより上がC
                    if ($this->field["SUB_D065"] == '1') { //1:D065登録科目フラグ
                        $total = array_sum($term_data);
                        $cnt = get_count($term_data);
                        if ($cnt == 3) { //１～３学期の場合
                            if ($total <= 44) {
                                $this->fieldsCalc[$name] = 11;
                            } elseif ($total <= 77) {
                                $this->fieldsCalc[$name] = 22;
                            } elseif ($total > 77) {
                                $this->fieldsCalc[$name] = 33;
                            } else {
                                $this->fieldsCalc[$name] = "";
                            }
                        } elseif ($cnt == 2) { //１・２学期の場合
                            if ($total <= 33) {
                                $this->fieldsCalc[$name] = 11;
                            } elseif ($total <= 55) {
                                $this->fieldsCalc[$name] = 22;
                            } elseif ($total > 55) {
                                $this->fieldsCalc[$name] = 33;
                            } else {
                                $this->fieldsCalc[$name] = "";
                            }
                        } else {
                            $this->fieldsCalc[$name] = "";
                        }
                        continue;
                    }
                    //合計・・・駿台甲府要望(総合点)
                    if ($this->Properties["KNJD128Y_SANSHUTU_BTN_SUM"] == '1') {
                        $this->fieldsCalc[$name] = array_sum($term_data); //合計
                        continue;
                    }
                    //$avg = array_sum($term_data)/get_count($term_data);
                    $avg = array_sum($term_data)/(array_sum($perfect_data)/100);
                    //評価の算出方法
                    if ($calcMethod == "3") {
                        $this->fieldsCalc[$name] = floor($avg); //3:切り捨て
                    } elseif ($calcMethod == "2") {
                        $this->fieldsCalc[$name] = ceil($avg);  //2:切り上げ
                    } else {
                        $this->fieldsCalc[$name] = round($avg); //1:四捨五入(初期値)
                    }
                    //欠席フラグの場合、空白とする
                    if ($abcentFlg) {
                        $this->fieldsCalc[$name] = ""; //空白
                    }
                } else {
                    $this->fieldsCalc[$name] = ""; //空白
                }
            }
        }

        if ($this->validateInput()) {
            $this->setMessage("", "算出しました。\\n更新ボタンを押下しないとデータは保存されません。");
        }
    }
}
//var_dump($_REQUEST);
