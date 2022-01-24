<?php

require_once('for_php7.php');
class knjd128vModel extends Model
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

    public function knjd128vModel()
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

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        //初期値の設定
        $this->usePerfect = (strlen($this->Properties["usePerfect"])) ? $this->Properties["usePerfect"] : false;
        $this->chikokuHyoujiFlg = (strlen($this->Properties["chikokuHyoujiFlg"])) ? $this->Properties["chikokuHyoujiFlg"] : 0;
        //「評価にリンクを付けるか？」のフラグを取得
        $rtnRow = knjd128vQuery::getNameMstD048();
        $this->useSlumpHyouka = $rtnRow["NAMESPARE1"] == "1" ? true : false; // 1:評価にリンクを付ける
        //「ＣＳＶは、非表示とする？」のフラグを取得
        $rtnRow = knjd128vQuery::getNameMstD058();
        $this->useCsv = $rtnRow["NAMESPARE1"] == "1" ? false : true; // 1:非表示
        //学校名取得
        $this->z010name1 = knjd128vQuery::getNameMstZ010();
        //欠席記号
        $this->kessekiMarks = ($this->z010name1 == "nichi-ni") ? array("*", "**") : array("*");
        //各種設定を取得・・・洛南要望
        $rtnRow = knjd128vQuery::getNameMstSettei("D067", "01");
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
            $this->field["CALC_TESTCD"] = VARS::post("CALC_TESTCD"); //上の算出ボタン用

            //学期評価・学年評価を算出
            //評価の算出方法(1:四捨五入 2:切り上げ 3:切り捨て)
            $rtnRow = knjd128vQuery::getCalcMethod();

            // 算出（山村学園用）
            if ($this->z010name1 == "yamamura") {
                // (現状)学年評価100段階の算出（賢者）
                if ($this->field["CALC_TESTCD"] == "9990007") {
                    $this->getCalcModel($rtnRow["SCHOOL_REMARK1"]);
                // (追加機能)各学期評価10段階の算出を追加（山村学園用）
                } else {
                    $this->getCalcModelYamamura($rtnRow["SCHOOL_REMARK1"]);
                }
                // 学期評価、学年評価の計算式を追加（花園用）
            } elseif ($this->z010name1 == "hanazono") {
                $this->getCalcModelHanazono($rtnRow["SCHOOL_REMARK1"]);
            // 学期評価の自動算出機能追加（専修大松戸用）
            } elseif ($this->z010name1 == "matsudo") {
                $this->getCalcModelMatsudo($rtnRow["SCHOOL_REMARK1"]);
            // プロパティーKNJD128V_SeisekiSanshutsuPatternが1の時、KNJD219Jの割合設定を参照し算出する。
            // 立命館慶祥から要望
            } elseif ($this->Properties["KNJD128V_SeisekiSanshutsuPattern"] == '1') {
                $this->getCalcModelPercent($rtnRow["SCHOOL_REMARK1"]);
            } else {
                $this->getCalcModel($rtnRow["SCHOOL_REMARK1"]);
            }
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
        if ($this->z010name1 == "nichi-ni") {
            //テスト種別が中間/期末素点/評価/評定以外の時にスコアが*または**ならエラー
            foreach ($this->testcdArray as $key => $codeArray) {
                $testcd = $codeArray["TESTCD"];
                $scoreDiv = substr($testcd, 5, 2);
                if (in_array($scoreDiv, array('01', '08', '09'))) {
                    continue;
                }

                for ($i = 0; $i < $this->field["COUNTER"]; $i++) {
                    $score = $this->fields["SCORE".$testcd][$i];
                    if ($score == "*" || ($this->z010name1 == "nichi-ni" && $score == "**")) {
                        $addStr = $this->z010name1 == "nichi-ni" ? "および**" : "" ;
                        $errMsg = "*".$addStr."は各学期の「素点」「評価」「評定」列にのみ入力してください。";

                        $this->setWarning("MSG901", $errMsg);
                        return false;
                    }
                }
            }
        }

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
        }

        if ($this->validateInput()) {
            knjd128vQuery::update($this);
            knjd128vQuery::updateRecordChkfin($this);
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
                if ($this->Properties["knjd128vShowBtnCalcOnTop"] == '1' && strlen($this->field["CALC_TESTCD"]) && $this->field["CALC_TESTCD"] != $testcd) {
                    continue;
                }
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
                if (count($term_data)) {
                    //総合的な学習の時間(D065登録科目)の年間評価算出・・・埼玉栄要望
                    //各学期評価：A = 11、B = 22、C = 33
                    //各学期評価を合計し、１～３学期の場合、44以下がA、77以下がB、それより上がC
                    //各学期評価を合計し、１・２学期の場合、33以下がA、55以下がB、それより上がC
                    if ($this->field["SUB_D065"] == '1') { //1:D065登録科目フラグ
                        $total = array_sum($term_data);
                        $cnt = count($term_data);
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
                    if ($this->Properties["KNJD128V_SANSHUTU_BTN_SUM"] == '1') {
                        $this->fieldsCalc[$name] = array_sum($term_data); //合計
                        continue;
                    }
                    //$avg = array_sum($term_data)/count($term_data);
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

    // プロパティーKNJD128V_SeisekiSanshutsuPatternが1の時、KNJD219Jの割合設定を参照し算出する。
    // 立命館慶祥から要望
    public function &getCalcModelPercent($calcMethod)
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
            $schno = VARS::post("SCHREGNO"."-".$i);
            //異動日の翌日以降は、転学、退学者は、入力不可にする。(算出用)
            $tentaigakuFlg = VARS::post("TENTAIGAKU_FLG"."-".$i);
            if ($tentaigakuFlg && AUTHORITY != DEF_UPDATABLE) {
                continue;
            }
            foreach ($this->testcdMotoArrayPercent[$schno] as $testcd => $array) {
                $score_percent = array();
                $score_data = array();
                $percent_data = array();
                //算出元
                foreach ($array as $key => $codeArray) {
                    $score = $this->fieldsCalc["SCORE".$codeArray["MOTO_TESTCD"]."-".$i];

                    if ($this->Properties["KNJD219J_SeisekiSanshutsuPattern"] == '1' && substr($codeArray["MOTO_TESTCD"], 5, 2) == "01") {
                        if (is_numeric($score)) {
                            $score_data[] = $score;
                        }

                        if (is_numeric($codeArray["PERCENT"])) {
                            $percent_data[] = $codeArray["PERCENT"];
                        }
                    } elseif (is_numeric($score) && is_numeric($codeArray["PERCENT"])) {
                        $score_percent[] = $score * $codeArray["PERCENT"];
                    }
                }
                //SDIV=01のテストを纏めて割合設定
                if ($this->Properties["KNJD219J_SeisekiSanshutsuPattern"] == '1' && count($score_data) && count($percent_data)) {
                    $score_percent[] = array_sum($score_data)/count($score_data) * array_sum($percent_data);
                }
                //算出先
                $name = "SCORE".$testcd."-".$i;
                $name9 = "SCORE"."9990008"."-".$i; //学年末成績(9-9900-08)にもセットする
                if (count($score_percent)) {
                    $avg = array_sum($score_percent)/100;
                    $this->fieldsCalc[$name] = round($avg); //1:四捨五入(初期値)
                    $this->fieldsCalc[$name9] = round($avg);
                } else {
                    $this->fieldsCalc[$name] = ""; //空白
                    $this->fieldsCalc[$name9] = "";
                }
            }
        }

        if ($this->validateInput()) {
            $this->setMessage("", "算出しました。\\n更新ボタンを押下しないとデータは保存されません。");
        }
    }

    //◆学年評価の算出を追加・・・各学期評価の平均
    //◆学期評価の自動算出機能（専修大松戸用）
    //管理者コントロールの算出設定画面で下記の通り設定されている前提
    //算出先・・・学期評価
    //算出元・・・中間素点、期末素点、平常点
    //見込点ではなく追指導を参照する
    //計算式内の掛け率は、単位マスタメンテで設定した値（設定例 0.9,0.8,0.7）
    public function &getCalcModelMatsudo($calcMethod)
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
        $setCalcFlg = false;
        //生徒のループ
        for ($i = 0; $i < $this->field["COUNTER"]; $i++) {
            //異動日の翌日以降は、転学、退学者は、入力不可にする。(算出用)
            $tentaigakuFlg = VARS::post("TENTAIGAKU_FLG"."-".$i);
            if ($tentaigakuFlg && AUTHORITY != DEF_UPDATABLE) {
                continue;
            }
            //掛け率
            $rate = VARS::post("RATE"."-".$i);
            //算出先のループ
            foreach ($this->testcdMotoArray as $testcd => $array) {
                if ($this->Properties["knjd128vShowBtnCalcOnTop"] == '1' && strlen($this->field["CALC_TESTCD"]) && $this->field["CALC_TESTCD"] != $testcd) {
                    continue;
                }
                $total = array();
                $heijou = array();
                //算出元のループ
                foreach ($array as $key => $codeArray) {
                    $semester = substr($codeArray["MOTO_TESTCD"], 0, 1);
                    $testkind = substr($codeArray["MOTO_TESTCD"], 1, 2);
                    $testitem = substr($codeArray["MOTO_TESTCD"], 3, 2);
                    $scorediv = substr($codeArray["MOTO_TESTCD"], 5, 2);
                    $scoreName = "SCORE".$codeArray["MOTO_TESTCD"];
                    $score = $this->fieldsCalc[$scoreName."-".$i];
                    $sidou = VARS::post($scoreName."_SIDOU_SCORE"."-".$i); //見込点ではなく追指導を参照する
                    //数値セット
                    if ($testcd != "9990008" && ($testkind == "01" && $scorediv == "01" || $testkind == "02" && $scorediv == "01")) {//中間or期末
                        //追指導を優先する
                        if (is_numeric($sidou)) {//追指導
                            $total[] = $sidou;
                        } elseif (is_numeric($score)) {//素点
                            $total[] = $score;
                        }
                    } elseif ($testcd != "9990008" && $testkind == "99" && $scorediv == "02") {//平常点
                        if (is_numeric($score)) {
                            $heijou[] = $score;
                        }
                    } elseif ($testcd == "9990008" && $testkind == "99" && $scorediv == "08") {//学期評価
                        if (is_numeric($score)) {
                            $total[] = $score;
                        }
                    }
                }
                //学期評価算出
                //算出先へセット
                if ($testcd != "9990008" && count($total) && count($heijou) && strlen($rate)) {
                    $avg = array_sum($total)/count($total) * $rate;
                    $setValue = round($avg) + array_sum($heijou);
                    $setName = "SCORE".$testcd."-".$i;
                    $this->fieldsCalc[$setName] = $setValue;
                    $setCalcFlg = true;
                //学年評価算出
                } elseif ($testcd == "9990008" && count($total)) {
                    $avg = array_sum($total)/count($total);
                    $setValue = round($avg);
                    $setName = "SCORE".$testcd."-".$i;
                    $this->fieldsCalc[$setName] = $setValue;
                    $setCalcFlg = true;
                }
            }
        }

        if ($setCalcFlg) {
            $this->setMessage("", "算出しました。\\n更新ボタンを押下しないとデータは保存されません。");
        } else {
            $this->setMessage("", "算出データはありません。");
        }
    }

    //◆学期評価、学年評価の計算式を追加（花園用）
    //・学期評価 = (中間素点 + 期末素点) / 2 + (平常点) (小数点以下切り捨て)
    //  但し、100を超える場合は100とし、マイナスとなる場合はゼロとする。
    //  平常点は-10～10を入力
    //・学年評価 = (1学期評価 + 2学期評価 + 3学期評価) / 3 (小数点以下切り捨て)
    public function &getCalcModelHanazono($calcMethod)
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
        $setCalcFlg = false;
        //生徒のループ
        for ($i = 0; $i < $this->field["COUNTER"]; $i++) {
            //異動日の翌日以降は、転学、退学者は、入力不可にする。(算出用)
            $tentaigakuFlg = VARS::post("TENTAIGAKU_FLG"."-".$i);
            if ($tentaigakuFlg && AUTHORITY != DEF_UPDATABLE) {
                continue;
            }
            //算出先のループ
            foreach ($this->testcdMotoArray as $testcd => $array) {
                if ($this->Properties["knjd128vShowBtnCalcOnTop"] == '1' && strlen($this->field["CALC_TESTCD"]) && $this->field["CALC_TESTCD"] != $testcd) {
                    continue;
                }
                $isGakkiHyouka = substr($testcd, 0, 1) != "9" ? true : false;
                $total = array();
                $heijou = array();
                //算出元のループ
                foreach ($array as $key => $codeArray) {
                    $testkind = substr($codeArray["MOTO_TESTCD"], 1, 2);
                    $scorediv = substr($codeArray["MOTO_TESTCD"], 5, 2);
                    $score = $this->fieldsCalc["SCORE".$codeArray["MOTO_TESTCD"]."-".$i];

                    //学期評価算出の場合
                    if ($isGakkiHyouka) {
                        //中間
                        if ($testkind == "01" && $scorediv == "01" && is_numeric($score)) {
                            $total[] = $score;
                        //期末
                        } elseif ($testkind == "02" && $scorediv == "01" && is_numeric($score)) {
                            $total[] = $score;
                        //平常点
                        } elseif ($testkind == "99" && $scorediv == "02" && is_numeric($score)) {
                            $heijou[] = $score;
                        }
                        //学年評価算出の場合
                    } else {
                        //学期評価
                        if ($testkind == "99" && $scorediv == "08" && is_numeric($score)) {
                            $total[] = $score;
                        }
                    }
                }
                //算出先へセット
                //学期評価算出
                if ($isGakkiHyouka && count($total) && count($heijou)) {
                    $avg = array_sum($total)/count($total);
                    $setValue = floor($avg) + array_sum($heijou); //切り捨て
                    if ($setValue > 100) {
                        $setValue = 100;
                    } elseif ($setValue < 0) {
                        $setValue = 0;
                    }
                    $this->fieldsCalc["SCORE".$testcd."-".$i] = $setValue;
                    $setCalcFlg = true;
                //学年評価算出
                } elseif (!$isGakkiHyouka && count($total)) {
                    $avg = array_sum($total)/count($total);
                    $setValue = floor($avg); //切り捨て
                    $this->fieldsCalc["SCORE".$testcd."-".$i] = $setValue;
                    $setCalcFlg = true;
                }
            }
        }

        if ($setCalcFlg) {
            $this->setMessage("", "算出しました。\\n更新ボタンを押下しないとデータは保存されません。");
        } else {
            $this->setMessage("", "算出データはありません。");
        }
    }

    //◆各学期評価10段階の自動算出（山村学園用）
    //換算値は評定マスタの評定区分「2:学期末用評価」に設定した値とする
    //①1学期-学期評価（1-99-00-08）[10段階] = 1期末（1-02-01-01）[100段階]からの算出
    //②2学期-学期評価（2-99-00-08）[10段階] = 2期末（2-02-01-01）[100段階]からの算出
    //③3学期-学期評価（3-99-00-08）[10段階] = 3期末（3-02-01-01）[100段階]からの算出
    //④3学期-学年評価10（9-99-00-08）[10段階] = 学年評価（9-99-00-07）[100段階]からの算出
    public function &getCalcModelYamamura($calcMethod)
    {
        //DB接続
        $db = Query::dbCheckOut();
        //評定マスタから換算値を取得
        $assessArray = array();
        $result = $db->query(knjd128vQuery::getAssessMst());
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $assessArray[] = $row;
        }
        $result->free();
        //DB切断
        Query::dbCheckIn($db);

        //初期化
        $this->fieldsCalc = array();
        //画面上から取得
        for ($i = 0; $i < $this->field["COUNTER"]; $i++) {
            foreach ($this->testcdArray as $key => $codeArray) {
                $name = "SCORE".$codeArray["TESTCD"]."-".$i;
                $this->fieldsCalc[$name] = VARS::post($name);
            }
        }
        $setCalcFlg = false;
        //生徒のループ
        for ($i = 0; $i < $this->field["COUNTER"]; $i++) {
            //異動日の翌日以降は、転学、退学者は、入力不可にする。(算出用)
            $tentaigakuFlg = VARS::post("TENTAIGAKU_FLG"."-".$i);
            if ($tentaigakuFlg && AUTHORITY != DEF_UPDATABLE) {
                continue;
            }
            //算出先のループ
            foreach ($this->testcdMotoArray as $testcd => $array) {
                if ($this->Properties["knjd128vShowBtnCalcOnTop"] == '1' && strlen($this->field["CALC_TESTCD"]) && $this->field["CALC_TESTCD"] != $testcd) {
                    continue;
                }
                $assesslevel = "";
                //算出元のループ
                foreach ($array as $key => $codeArray) {
                    $scoreName = "SCORE".$codeArray["MOTO_TESTCD"]."-".$i;
                    $score = $this->fieldsCalc[$scoreName];
                    //評定マスタのループ
                    foreach ($assessArray as $key => $assessRow) {
                        //数値セット(換算)
                        if (is_numeric($score) && $score >= $assessRow["ASSESSLOW"] && $score <= $assessRow["ASSESSHIGH"]) {
                            $assesslevel = $assessRow["ASSESSLEVEL"];
                        }
                    }
                }
                //算出先へセット
                if (strlen($assesslevel)) {
                    $setValue = $assesslevel;
                    $setName = "SCORE".$testcd."-".$i;
                    $this->fieldsCalc[$setName] = $setValue;
                    $setCalcFlg = true;
                }
            }
        }

        if ($setCalcFlg) {
            $this->setMessage("", "算出しました。\\n更新ボタンを押下しないとデータは保存されません。");
        } else {
            $this->setMessage("", "算出データはありません。");
        }
    }
}
