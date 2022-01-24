<?php

require_once('for_php7.php');

class knjf164Model extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $control;
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjf164Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }
    public function init()
    {
        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "DATA_KIND"        =>  VARS::post("DATA_KIND"),        //対象データ
                             "YEAR"             =>  VARS::post("YEAR"),             //年度一覧
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組一覧
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                             );

        $this->cmd  = VARS::request("cmd");
        $this->file = VARS::file("FILE");

        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }
    public function &getMainModel()
    {
    }

    //実行モデルを返します
    public function &getExecModel()
    {
        //ファイル名取得
        $target = $_FILES['FILE']['name'];
        $fileInfo = pathinfo($target);
        $kakutyousi = $fileInfo['extension'];
        $kakutyousi = strtoupper($kakutyousi);
        //拡張子チェック
        if ($kakutyousi != "CSV") {
            $this->warning = "CSVファイルを指定して下さい。\\n(".$kakutyousi."ファイルの取込は出来ません。)";
            return false;
        }

        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                if ($this->field["DATA_KIND"] == "1") {
                    if ($this->field["SHORI_MEI"] == "1") {
                        knjf164Query::insertQueryCsv1($this, $this->data_arr);
                    } else {
                        knjf164Query::deleteQueryCsv1($this, $this->data_arr);
                    }
                } elseif ($this->field["DATA_KIND"] == "2") {
                    if ($this->field["SHORI_MEI"] == "1") {
                        knjf164Query::insertQueryCsv2($this, $this->data_arr);
                    } else {
                        knjf164Query::deleteQueryCsv2($this, $this->data_arr);
                    }
                } elseif ($this->field["DATA_KIND"] == "3") {
                    if ($this->field["SHORI_MEI"] == "1") {
                        knjf164Query::insertQueryCsv3($this, $this->data_arr);
                    } else {
                        knjf164Query::deleteQueryCsv3($this, $this->data_arr);
                    }
                } elseif ($this->field["DATA_KIND"] == "4") {
                    if ($this->field["SHORI_MEI"] == "1") {
                        knjf164Query::insertQueryCsv4($this, $this->data_arr);
                    } else {
                        knjf164Query::deleteQueryCsv4($this, $this->data_arr);
                    }
                } elseif ($this->field["DATA_KIND"] == "5_1") {
                    if ($this->field["SHORI_MEI"] == "1") {
                        knjf164Query::insertQueryCsv5_1($this, $this->data_arr);
                    } else {
                        knjf164Query::deleteQueryCsv5_1($this, $this->data_arr);
                    }
                } elseif ($this->field["DATA_KIND"] == "5_2") {
                    if ($this->field["SHORI_MEI"] == "1") {
                        knjf164Query::insertQueryCsv5_2($this, $this->data_arr);
                    } else {
                        knjf164Query::deleteQueryCsv5_2($this, $this->data_arr);
                    }
                } elseif ($this->field["DATA_KIND"] == "6") {
                    if ($this->field["SHORI_MEI"] == "1") {
                        knjf164Query::insertQueryCsv6($this, $this->data_arr);
                    } else {
                        knjf164Query::deleteQueryCsv6($this, $this->data_arr);
                    }
                }
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //取込
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            if ($this->field["DATA_KIND"] == "1") {
                $checkdata = array("SCHREGNO"       => array(8,"学籍番号"),
                                   "SEQ"            => array(2,"連番"),
                                   "DISEASE"        => array(60,"病名"),
                                   "S_YEAR"         => array(4,"期間（開始年）"),
                                   "S_MONTH"        => array(2,"期間（開始月）"),
                                   "E_YEAR"         => array(4,"期間（終了年）"),
                                   "E_MONTH"        => array(2,"期間（終了月）"),
                                   "SITUATION"      => array(120,"経過状況"));
            } elseif ($this->field["DATA_KIND"] == "2") {
                $checkdata = array("SCHREGNO"       => array(8,"学籍番号"),
                                   "SEQ"            => array(2,"連番"),
                                   "DISEASE"        => array(60,"病名"),
                                   "S_YEAR"         => array(4,"期間（開始年）"),
                                   "S_MONTH"        => array(2,"期間（開始月）"),
                                   "E_YEAR"         => array(4,"期間（終了年）"),
                                   "E_MONTH"        => array(2,"期間（終了月）"),
                                   "HOSPITAL"       => array(60,"病院名"),
                                   "DOCTOR"         => array(60,"主治医"),
                                   "TELNO"          => array(14,"電話番号"),
                                   "MEDICINE"       => array(60,"服用薬"),
                                   "SITUATION"      => array(120,"経過状況"));
            } elseif ($this->field["DATA_KIND"] == "3") {
                $checkdata = array("SCHREGNO"       => array(8,"学籍番号"),
                                   "RELANO"         => array(2,"親族番号"),
                                   "RELANAME"       => array(60,"親族氏名"),
                                   "RELAKANA"       => array(120,"親族氏名かな"),
                                   "RELATIONSHIP"   => array(2,"親族続柄"),
                                   "RELABIRTHDAY"   => array(10,"親族生年月日"),
                                   "REMARK"         => array(90,"健康状態（備考）"));
            } elseif ($this->field["DATA_KIND"] == "4") {
                $checkdata = array("SCHREGNO"               => array(8,"学籍番号",1),
                                   "ALLERGY_MEDICINE"       => array(120,"薬アレルギー",0),
                                   "ALLERGY_FOOD"           => array(120,"食品アレルギー",0),
                                   "ALLERGY_OTHER"          => array(120,"その他アレルギー",0),
                                   "BLOOD"                  => array(2,"血液型",0),
                                   "RH"                     => array(1,"Ｒｈ",0),
                                   "MEASLES_AGE"            => array(2,"麻疹（年齢）",1),
                                   "G_MEASLES_AGE"          => array(2,"風疹（年齢）",1),
                                   "VARICELLA_AGE"          => array(2,"水痘（年齢）",1),
                                   "OTITIS_MEDIA_AGE"       => array(2,"中耳炎（年齢）",1),
                                   "TB_AGE"                 => array(2,"結核（年齢）",1),
                                   "KAWASAKI_AGE"           => array(2,"川崎病（年齢）",1),
                                   "INFECTION_AGE"          => array(2,"溶連菌感染症（年齢）",1),
                                   "MUMPS_AGE"              => array(2,"流行性耳下腺炎（年齢）",1),
                                   "HEART_DISEASE"          => array(45,"心臓疾患（病名）",0),
                                   "HEART_S_AGE"            => array(2,"心臓疾患（年齢）",1),
                                   "HEART_SITUATION"        => array(2,"心臓疾患（経過）",1),
                                   "HEART_E_AGE"            => array(2,"心臓疾患（全治年齢）",1),
                                   "KIDNEY_DISEASE"         => array(45,"腎臓疾患（病名）",0),
                                   "KIDNEY_S_AGE"           => array(2,"腎臓疾患（年齢）",1),
                                   "KIDNEY_SITUATION"       => array(2,"腎臓疾患（経過）",1),
                                   "KIDNEY_E_AGE"           => array(2,"腎臓疾患（全治年齢）",1),
                                   "ASTHMA_S_AGE"           => array(2,"ぜんそく（年齢）",1),
                                   "ASTHMA_SITUATION"       => array(2,"ぜんそく（経過）",1),
                                   "ASTHMA_E_AGE"           => array(2,"ぜんそく（全治年齢）",1),
                                   "CONVULSIONS_S_AGE"      => array(2,"けいれん（年齢）",1),
                                   "CONVULSIONS_SITUATION"  => array(2,"けいれん（経過）",1),
                                   "CONVULSIONS_E_AGE"      => array(2,"けいれん（全治年齢）",1),
                                   "OTHER_DISEASE"          => array(120,"その他（病名）"),
                                   "TUBERCULIN"             => array(2,"ツベルクリン（接種）",1),
                                   "TUBERCULIN_YEAR"        => array(4,"ツベルクリン（年）",1),
                                   "TUBERCULIN_MONTH"       => array(2,"ツベルクリン（月）",1),
                                   "TUBERCULIN_JUDGE"       => array(1,"ツベルクリン（判定）",1),
                                   "BCG"                    => array(2,"ＢＣＧ（接種）",1),
                                   "BCG_YEAR"               => array(4,"ＢＣＧ（年）",1),
                                   "BCG_MONTH"              => array(2,"ＢＣＧ（月）",1),
                                   "POLIO"                  => array(2,"ポリオ（接種）",1),
                                   "POLIO_YEAR"             => array(4,"ポリオ（年）",1),
                                   "POLIO_MONTH"            => array(2,"ポリオ（月）",1),
                                   "G_MEASLES"              => array(2,"風疹（接種）",1),
                                   "G_MEASLES_YEAR"         => array(4,"風疹（年）",1),
                                   "G_MEASLES_MONTH"        => array(2,"風疹（月）",1),
                                   "VARICELLA"              => array(2,"水痘（接種）",1),
                                   "VARICELLA_YEAR"         => array(4,"水痘（年）",1),
                                   "VARICELLA_MONTH"        => array(2,"水痘（月）",1),
                                   "MUMPS"                  => array(2,"流行性耳下腺炎（接種）",1),
                                   "MUMPS_YEAR"             => array(4,"流行性耳下腺炎（年）",1),
                                   "MUMPS_MONTH"            => array(2,"流行性耳下腺炎（月）",1),
                                   "ENCEPHALITIS"           => array(2,"日本脳炎（接種）",1),
                                   "ENCEPHALITIS_YEAR1"     => array(4,"日本脳炎（１回・年）",1),
                                   "ENCEPHALITIS_MONTH1"    => array(2,"日本脳炎（１回・月）",1),
                                   "ENCEPHALITIS_YEAR2"     => array(4,"日本脳炎（２回・年）",1),
                                   "ENCEPHALITIS_MONTH2"    => array(2,"日本脳炎（２回・月）",1),
                                   "ENCEPHALITIS_YEAR3"     => array(4,"日本脳炎（３回・年）",1),
                                   "ENCEPHALITIS_MONTH3"    => array(2,"日本脳炎（３回・月）",1),
                                   "ENCEPHALITIS_YEAR4"     => array(4,"日本脳炎（小４・年）",1),
                                   "ENCEPHALITIS_MONTH4"    => array(2,"日本脳炎（小４・月）",1),
                                   "ENCEPHALITIS_YEAR5"     => array(4,"日本脳炎（中２・年）",1),
                                   "ENCEPHALITIS_MONTH5"    => array(2,"日本脳炎（中２・月）",1),
                                   "MIXED"                  => array(2,"二種混合（接種）",1),
                                   "MIXED_YEAR1"            => array(4,"二種混合（１回・年）",1),
                                   "MIXED_MONTH1"           => array(2,"二種混合（１回・月）",1),
                                   "MIXED_YEAR2"            => array(4,"二種混合（２回・年）",1),
                                   "MIXED_MONTH2"           => array(2,"二種混合（２回・月）",1),
                                   "MIXED_YEAR3"            => array(4,"二種混合（３回・年）",1),
                                   "MIXED_MONTH3"           => array(2,"二種混合（３回・月）",1),
                                   "MIXED_YEAR4"            => array(4,"二種混合（追加・年）",1),
                                   "MIXED_MONTH4"           => array(2,"二種混合（追加・月）",1),
                                   "MIXED_YEAR5"            => array(4,"二種混合（小６・年）",1),
                                   "MIXED_MONTH5"           => array(2,"二種混合（小６・月）",1),
                                   "MEASLES"                => array(1,"麻疹（接種）",1),
                                   "MEASLES_TIMES"          => array(2,"麻疹（回数）",1),
                                   "MEASLES_YEAR1"          => array(4,"麻疹（１回・年）",1),
                                   "MEASLES_MONTH1"         => array(2,"麻疹（１回・月）",1),
                                   "MEASLES_YEAR2"          => array(4,"麻疹（２回・年）",1),
                                   "MEASLES_MONTH2"         => array(2,"麻疹（２回・月）",1),
                                   "MEASLES_YEAR3"          => array(4,"麻疹（３回・年）",1),
                                   "MEASLES_MONTH3"         => array(2,"麻疹（３回・月）",1),
                                   "VACCINE"                => array(2,"ワクチンの種類",1),
                                   "LOT_NO"                 => array(10,"ロット番号"),
                                   "CONFIRMATION"           => array(2,"確認方法",1),
                                   "A_MEASLES"              => array(1,"麻疹（罹患歴）",1),
                                   "A_MEASLES_AGE"          => array(2,"麻疹（罹患歴・年齢）",1),
                                   "A_CONFIRMATION"         => array(2,"確認方法（罹患歴）",1),
                                   "ANTIBODY"               => array(1,"抗体検査",1),
                                   "ANTIBODY_YEAR"          => array(4,"検査日（年）",1),
                                   "ANTIBODY_MONTH"         => array(2,"検査日（月）",1),
                                   "ANTIBODY_POSITIVE"      => array(1,"抗体有無",1));
            } elseif ($this->field["DATA_KIND"] == "5_1") {
                $checkdata = array("SCHREGNO"       => array(8,"学籍番号"),
                                   "INVEST_YEAR"    => array(4,"調査年度"),
                                   "E_YEAR"         => array(4,"記入日（年）"),
                                   "E_MONTH"        => array(2,"記入日（月）"));

                $db = Query::dbCheckOut();
                $result = $db->query(knjf164Query::getHealthQuestion());
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $checkdata["ANSWER".$row["QUESTIONCD"]] = array(1,$row["CONTENTS"]);
                }
                $result->free();
                Query::dbCheckIn($db);
            } elseif ($this->field["DATA_KIND"] == "5_2") {
                $checkdata = array("SCHREGNO"       => array(8,"学籍番号"),
                                   "ATTENTION"      => array(600,"注意すべき点"));
            } elseif ($this->field["DATA_KIND"] == "6") {
                $checkdata = array("SCHREGNO"           => array(8,"学籍番号"),
                                   "INSURED_NAME"       => array(60,"被保険者氏名"),
                                   "INSURED_MARK"       => array(60,"加入者記号"),
                                   "INSURED_NO"         => array(20,"加入者番号"),
                                   "INSURANCE_NAME"     => array(60,"保険者名称"),
                                   "INSURANCE_NO"       => array(20,"保険者番号"),
                                   "VALID_DATE"         => array(10,"有効期限"),
                                   "AUTHORIZE_DATE"     => array(10,"被扶養者認定年月"),
                                   "RELATIONSHIP"       => array(2,"続柄"),
                                   "REMARK"             => array(1200,"保健室記入用備考"),
                                   "ATTENTION"          => array(90,"山上・海浜学校のための注意点"));
            }

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query =  $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1 = array();
            $primaryKey2 = array();
            $duplicateCsv1 = array();
            $duplicateCsv2 = array();
            if ($this->field["DATA_KIND"] == "3") {
                $primaryKey1 = $this->getPrimaryKey('SCHREG_RELA_DAT');
                $primaryKey2 = $this->getPrimaryKey('HEALTH_RELA_DAT');
            } elseif ($this->field["DATA_KIND"] == "4") {
                $primaryKey1 = $this->getPrimaryKey('HEALTH_INVEST_OTHER_DAT');
            } elseif ($this->field["DATA_KIND"] == "5_1") {
                $primaryKey1 = $this->getPrimaryKey('HEALTH_INVEST_DAT');
            } elseif ($this->field["DATA_KIND"] == "5_2") {
                $primaryKey1 = $this->getPrimaryKey('HEALTH_INVEST_ATTENTION_DAT');
            } elseif ($this->field["DATA_KIND"] == "6") {
                $primaryKey1 = $this->getPrimaryKey('HEALTH_NURSE_ENT_DAT');
            }

            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADER"] == "on") {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    $tmp    = str_replace("↓", "\n", $tmp);

                    if ($this->field["DATA_KIND"] == "1") {
                        $data = array("SCHREGNO"        => (strlen(trim($tmp[4]))) ? sprintf("%08d", $tmp[4]) : "",
                                      "SEQ"             => (strlen(trim($tmp[6]))) ? $tmp[6] : "",
                                      "DISEASE"         => (strlen(trim($tmp[7]))) ? $tmp[7] : "",
                                      "S_YEAR"          => (strlen(trim($tmp[8]))) ? $tmp[8] : "",
                                      "S_MONTH"         => (strlen(trim($tmp[9]))) ? intval($tmp[9]) : "",
                                      "E_YEAR"          => (strlen(trim($tmp[10]))) ? $tmp[10] : "",
                                      "E_MONTH"         => (strlen(trim($tmp[11]))) ? intval($tmp[11]) : "",
                                      "SITUATION"       => (strlen(trim($tmp[12]))) ? $tmp[12] : "");
                    } elseif ($this->field["DATA_KIND"] == "2") {
                        $data = array("SCHREGNO"        => (strlen(trim($tmp[4]))) ? sprintf("%08d", $tmp[4]) : "",
                                      "SEQ"             => (strlen(trim($tmp[6]))) ? $tmp[6] : "",
                                      "DISEASE"         => (strlen(trim($tmp[7]))) ? $tmp[7] : "",
                                      "S_YEAR"          => (strlen(trim($tmp[8]))) ? $tmp[8] : "",
                                      "S_MONTH"         => (strlen(trim($tmp[9]))) ? intval($tmp[9]) : "",
                                      "E_YEAR"          => (strlen(trim($tmp[10]))) ? $tmp[10] : "",
                                      "E_MONTH"         => (strlen(trim($tmp[11]))) ? intval($tmp[11]) : "",
                                      "SUSPECT"         => (strlen(trim($tmp[12]))) ? $tmp[12] : "",
                                      "HOSPITAL"        => (strlen(trim($tmp[13]))) ? $tmp[13] : "",
                                      "DOCTOR"          => (strlen(trim($tmp[14]))) ? $tmp[14] : "",
                                      "TELNO"           => (strlen(trim($tmp[15]))) ? $tmp[15] : "",
                                      "MEDICINE"        => (strlen(trim($tmp[16]))) ? $tmp[16] : "",
                                      "SITUATION"       => (strlen(trim($tmp[17]))) ? $tmp[17] : "");
                    } elseif ($this->field["DATA_KIND"] == "3") {
                        $data = array("SCHREGNO"        => (strlen(trim($tmp[4]))) ? sprintf("%08d", $tmp[4]) : "",
                                      "RELANO"          => (strlen(trim($tmp[6]))) ? $tmp[6] : "",
                                      "RELANAME"        => (strlen(trim($tmp[7]))) ? $tmp[7] : "",
                                      "RELAKANA"        => (strlen(trim($tmp[8]))) ? $tmp[8] : "",
                                      "RELATIONSHIP"    => (strlen(trim($tmp[9]))) ? sprintf("%02d", $tmp[9]) : "",
                                      "RELABIRTHDAY"    => (strlen(trim($tmp[10]))) ? $tmp[10] : "",
                                      "REMARK"          => (strlen(trim($tmp[11]))) ? $tmp[11] : "");
                    } elseif ($this->field["DATA_KIND"] == "4") {
                        $data = array("SCHREGNO"                => (strlen(trim($tmp[4]))) ? sprintf("%08d", $tmp[4]) : "",
                                      "ALLERGY_MEDICINE"        => (strlen(trim($tmp[6]))) ? $tmp[6] : "",
                                      "ALLERGY_FOOD"            => (strlen(trim($tmp[7]))) ? $tmp[7] : "",
                                      "ALLERGY_OTHER"           => (strlen(trim($tmp[8]))) ? $tmp[8] : "",
                                      "BLOOD"                   => (strlen(trim($tmp[9]))) ? strtoupper($tmp[9]) : "",
                                      "RH"                      => (strlen(trim($tmp[10]))) ? $tmp[10] : "",
                                      "MEASLES_AGE"             => (strlen(trim($tmp[11]))) ? intval($tmp[11]) : "",
                                      "G_MEASLES_AGE"           => (strlen(trim($tmp[12]))) ? intval($tmp[12]) : "",
                                      "VARICELLA_AGE"           => (strlen(trim($tmp[13]))) ? intval($tmp[13]) : "",
                                      "OTITIS_MEDIA_AGE"        => (strlen(trim($tmp[14]))) ? intval($tmp[14]) : "",
                                      "TB_AGE"                  => (strlen(trim($tmp[15]))) ? intval($tmp[15]) : "",
                                      "KAWASAKI_AGE"            => (strlen(trim($tmp[16]))) ? intval($tmp[16]) : "",
                                      "INFECTION_AGE"           => (strlen(trim($tmp[17]))) ? intval($tmp[17]) : "",
                                      "MUMPS_AGE"               => (strlen(trim($tmp[18]))) ? intval($tmp[18]) : "",
                                      "HEART_DISEASE"           => (strlen(trim($tmp[19]))) ? $tmp[19] : "",
                                      "HEART_S_AGE"             => (strlen(trim($tmp[20]))) ? intval($tmp[20]) : "",
                                      "HEART_SITUATION"         => (strlen(trim($tmp[21]))) ? $tmp[21] : "",
                                      "HEART_E_AGE"             => (strlen(trim($tmp[22]))) ? intval($tmp[22]) : "",
                                      "KIDNEY_DISEASE"          => (strlen(trim($tmp[23]))) ? $tmp[23] : "",
                                      "KIDNEY_S_AGE"            => (strlen(trim($tmp[24]))) ? intval($tmp[24]) : "",
                                      "KIDNEY_SITUATION"        => (strlen(trim($tmp[25]))) ? $tmp[25] : "",
                                      "KIDNEY_E_AGE"            => (strlen(trim($tmp[26]))) ? intval($tmp[26]) : "",
                                      "ASTHMA_S_AGE"            => (strlen(trim($tmp[27]))) ? intval($tmp[27]) : "",
                                      "ASTHMA_SITUATION"        => (strlen(trim($tmp[28]))) ? $tmp[28] : "",
                                      "ASTHMA_E_AGE"            => (strlen(trim($tmp[29]))) ? intval($tmp[29]) : "",
                                      "CONVULSIONS_S_AGE"       => (strlen(trim($tmp[30]))) ? intval($tmp[30]) : "",
                                      "CONVULSIONS_SITUATION"   => (strlen(trim($tmp[31]))) ? $tmp[31] : "",
                                      "CONVULSIONS_E_AGE"       => (strlen(trim($tmp[32]))) ? intval($tmp[32]) : "",
                                      "OTHER_DISEASE"           => (strlen(trim($tmp[33]))) ? $tmp[33] : "",
                                      "TUBERCULIN"              => (strlen(trim($tmp[34]))) ? $tmp[34] : "",
                                      "TUBERCULIN_YEAR"         => (strlen(trim($tmp[35]))) ? $tmp[35] : "",
                                      "TUBERCULIN_MONTH"        => (strlen(trim($tmp[36]))) ? intval($tmp[36]) : "",
                                      "TUBERCULIN_JUDGE"        => (strlen(trim($tmp[37]))) ? $tmp[37] : "",
                                      "BCG"                     => (strlen(trim($tmp[38]))) ? $tmp[38] : "",
                                      "BCG_YEAR"                => (strlen(trim($tmp[39]))) ? $tmp[39] : "",
                                      "BCG_MONTH"               => (strlen(trim($tmp[40]))) ? intval($tmp[40]) : "",
                                      "POLIO"                   => (strlen(trim($tmp[41]))) ? $tmp[41] : "",
                                      "POLIO_YEAR"              => (strlen(trim($tmp[42]))) ? $tmp[42] : "",
                                      "POLIO_MONTH"             => (strlen(trim($tmp[43]))) ? intval($tmp[43]) : "",
                                      "G_MEASLES"               => (strlen(trim($tmp[44]))) ? $tmp[44] : "",
                                      "G_MEASLES_YEAR"          => (strlen(trim($tmp[45]))) ? $tmp[45] : "",
                                      "G_MEASLES_MONTH"         => (strlen(trim($tmp[46]))) ? intval($tmp[46]) : "",
                                      "VARICELLA"               => (strlen(trim($tmp[47]))) ? $tmp[47] : "",
                                      "VARICELLA_YEAR"          => (strlen(trim($tmp[48]))) ? $tmp[48] : "",
                                      "VARICELLA_MONTH"         => (strlen(trim($tmp[49]))) ? intval($tmp[49]) : "",
                                      "MUMPS"                   => (strlen(trim($tmp[50]))) ? $tmp[50] : "",
                                      "MUMPS_YEAR"              => (strlen(trim($tmp[51]))) ? $tmp[51] : "",
                                      "MUMPS_MONTH"             => (strlen(trim($tmp[52]))) ? intval($tmp[52]) : "",
                                      "ENCEPHALITIS"            => (strlen(trim($tmp[53]))) ? $tmp[53] : "",
                                      "ENCEPHALITIS_YEAR1"      => (strlen(trim($tmp[54]))) ? $tmp[54] : "",
                                      "ENCEPHALITIS_MONTH1"     => (strlen(trim($tmp[55]))) ? intval($tmp[55]) : "",
                                      "ENCEPHALITIS_YEAR2"      => (strlen(trim($tmp[56]))) ? $tmp[56] : "",
                                      "ENCEPHALITIS_MONTH2"     => (strlen(trim($tmp[57]))) ? intval($tmp[57]) : "",
                                      "ENCEPHALITIS_YEAR3"      => (strlen(trim($tmp[58]))) ? $tmp[58] : "",
                                      "ENCEPHALITIS_MONTH3"     => (strlen(trim($tmp[59]))) ? intval($tmp[59]) : "",
                                      "ENCEPHALITIS_YEAR4"      => (strlen(trim($tmp[60]))) ? $tmp[60] : "",
                                      "ENCEPHALITIS_MONTH4"     => (strlen(trim($tmp[61]))) ? intval($tmp[61]) : "",
                                      "ENCEPHALITIS_YEAR5"      => (strlen(trim($tmp[62]))) ? $tmp[62] : "",
                                      "ENCEPHALITIS_MONTH5"     => (strlen(trim($tmp[63]))) ? intval($tmp[63]) : "",
                                      "MIXED"                   => (strlen(trim($tmp[64]))) ? $tmp[64] : "",
                                      "MIXED_YEAR1"             => (strlen(trim($tmp[65]))) ? $tmp[65] : "",
                                      "MIXED_MONTH1"            => (strlen(trim($tmp[66]))) ? intval($tmp[66]) : "",
                                      "MIXED_YEAR2"             => (strlen(trim($tmp[67]))) ? $tmp[67] : "",
                                      "MIXED_MONTH2"            => (strlen(trim($tmp[68]))) ? intval($tmp[68]) : "",
                                      "MIXED_YEAR3"             => (strlen(trim($tmp[69]))) ? $tmp[69] : "",
                                      "MIXED_MONTH3"            => (strlen(trim($tmp[70]))) ? intval($tmp[70]) : "",
                                      "MIXED_YEAR4"             => (strlen(trim($tmp[71]))) ? $tmp[71] : "",
                                      "MIXED_MONTH4"            => (strlen(trim($tmp[72]))) ? intval($tmp[72]) : "",
                                      "MIXED_YEAR5"             => (strlen(trim($tmp[73]))) ? $tmp[73] : "",
                                      "MIXED_MONTH5"            => (strlen(trim($tmp[74]))) ? intval($tmp[74]) : "",
                                      "MEASLES"                 => (strlen(trim($tmp[75]))) ? $tmp[75] : "",
                                      "MEASLES_TIMES"           => (strlen(trim($tmp[76]))) ? $tmp[76] : "",
                                      "MEASLES_YEAR1"           => (strlen(trim($tmp[77]))) ? $tmp[77] : "",
                                      "MEASLES_MONTH1"          => (strlen(trim($tmp[78]))) ? intval($tmp[78]) : "",
                                      "MEASLES_YEAR2"           => (strlen(trim($tmp[79]))) ? $tmp[79] : "",
                                      "MEASLES_MONTH2"          => (strlen(trim($tmp[80]))) ? intval($tmp[80]) : "",
                                      "MEASLES_YEAR3"           => (strlen(trim($tmp[81]))) ? $tmp[81] : "",
                                      "MEASLES_MONTH3"          => (strlen(trim($tmp[82]))) ? intval($tmp[82]) : "",
                                      "VACCINE"                 => (strlen(trim($tmp[83]))) ? $tmp[83] : "",
                                      "LOT_NO"                  => (strlen(trim($tmp[84]))) ? $tmp[84] : "",
                                      "CONFIRMATION"            => (strlen(trim($tmp[85]))) ? $tmp[85] : "",
                                      "A_MEASLES"               => (strlen(trim($tmp[86]))) ? $tmp[86] : "",
                                      "A_MEASLES_AGE"           => (strlen(trim($tmp[87]))) ? intval($tmp[87]) : "",
                                      "A_CONFIRMATION"          => (strlen(trim($tmp[88]))) ? $tmp[88] : "",
                                      "ANTIBODY"                => (strlen(trim($tmp[89]))) ? $tmp[89] : "",
                                      "ANTIBODY_YEAR"           => (strlen(trim($tmp[90]))) ? $tmp[90] : "",
                                      "ANTIBODY_MONTH"          => (strlen(trim($tmp[91]))) ? intval($tmp[91]) : "",
                                      "ANTIBODY_POSITIVE"       => (strlen(trim($tmp[92]))) ? $tmp[92] : "");
                    } elseif ($this->field["DATA_KIND"] == "5_1") {
                        $data = array("SCHREGNO"        => (strlen(trim($tmp[4]))) ? sprintf("%08d", $tmp[4]) : "",
                                      "INVEST_YEAR"     => (strlen(trim($tmp[6]))) ? $tmp[6] : "",
                                      "E_YEAR"          => (strlen(trim($tmp[7]))) ? $tmp[7] : "",
                                      "E_MONTH"         => (strlen(trim($tmp[8]))) ? intval($tmp[8]) : "");

                        $result = $db->query(knjf164Query::getHealthQuestion());
                        $j = 9;
                        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                            $data["ANSWER".$row["QUESTIONCD"]] = (strlen(trim($tmp[$j]))) ? $tmp[$j] : "";
                            $j++;
                        }
                        $result->free();
                    } elseif ($this->field["DATA_KIND"] == "5_2") {
                        $data = array("SCHREGNO"        => (strlen(trim($tmp[4]))) ? sprintf("%08d", $tmp[4]) : "",
                                      "ATTENTION"       => (strlen(trim($tmp[6]))) ? $tmp[6] : "");
                    } elseif ($this->field["DATA_KIND"] == "6") {
                        $data = array("SCHREGNO"        => (strlen(trim($tmp[4]))) ? sprintf("%08d", $tmp[4]) : "",
                                      "INSURED_NAME"    => (strlen(trim($tmp[6]))) ? $tmp[6] : "",
                                      "INSURED_MARK"    => (strlen(trim($tmp[7]))) ? $tmp[7] : "",
                                      "INSURED_NO"      => (strlen(trim($tmp[8]))) ? $tmp[8] : "",
                                      "INSURANCE_NAME"  => (strlen(trim($tmp[9]))) ? $tmp[9] : "",
                                      "INSURANCE_NO"    => (strlen(trim($tmp[10]))) ? $tmp[10] : "",
                                      "VALID_DATE"      => (strlen(trim($tmp[11]))) ? $tmp[11] : "",
                                      "AUTHORIZE_DATE"  => (strlen(trim($tmp[12]))) ? $tmp[12] : "",
                                      "RELATIONSHIP"    => (strlen(trim($tmp[13]))) ? sprintf("%02d", $tmp[13]) : "",
                                      "REMARK"          => (strlen(trim($tmp[14]))) ? $tmp[14] : "",
                                      "ATTENTION"       => (strlen(trim($tmp[15]))) ? $tmp[15] : "");
                    }
                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
                    }

                    foreach ($primaryKey2 as $key => $val) {
                        $duplicateCsv2[$line][$val] = $data[$val];
                    }
                    $line++;
                }
            }
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        }

        return $temp_arr;
    }

    //エラーチェック
    public function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)
    {
        $returnFlg  = true;
        $hissuErrFlg        = false;     //必須エラーフラグ
        $numericErrFlg      = false;     //数字エラーフラグ
        $lengthErrFlg       = false;     //レングスエラーフラグ
        $limitErrFlg        = false;     //範囲エラーフラグ
        $sonzairrFlg        = false;     //存在エラーフラグ
        $duplicateCsvErrFlg = false;     //重複エラーフラグ

        $hissuErrMsg        = "";        //必須エラーメッセージ
        $numericErrMsg      = "";        //数字エラーメッセージ
        $lengthErrMsg       = "";        //レングスエラーメッセージ
        $limitErrMsg        = "";        //範囲エラーメッセージ
        $sonzaiErrMsg       = "";        //存在エラーメッセージ
        $duplicateCsvErrMsg = "";        //重複エラーメッセージ

        $hissuSep           = "";
        $numericSep         = "";
        $lengthSep          = "";
        $limitSep           = "";
        $sonzaiSep          = "";
        $duplicateCsvSep    = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }
        if (sizeof($primaryKey2) > 0) {
            $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data);
            if ($dupFlg2[0] == 1) {
                $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg2[1] ."行目)");
            }
        }

        if ($this->field["DATA_KIND"] == "1" || $this->field["DATA_KIND"] == "2") {
            foreach ($data_each as $key => $val) {
                //必須項目チェック
                if ($this->field["SHORI_MEI"] == "1") {
                    if (in_array($key, array("SCHREGNO"))) {
                        if (!strlen(trim($data[$key]))) {
                            $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                        }
                    }
                } else {
                    if (in_array($key, array("SCHREGNO","SEQ"))) {
                        if (!strlen(trim($data[$key]))) {
                            $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                        }
                    }
                }

                //数値チェック
                if (in_array($key, array("SCHREGNO","SEQ","S_YEAR","S_MONTH","E_YEAR","E_MONTH"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }

                if ($this->field["SHORI_MEI"] == "1") {
                    //文字数チェック
                    if (strlen($data[$key]) > $val[0]) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }

                    //範囲チェック
                    if (in_array($key, array("S_YEAR","E_YEAR")) && $data[$key] != "" && ($data[$key] < 1900 || $data[$key] > 2100)) {
                        $returnFlg = $this->errSet($limitErrFlg, $limitErrMsg, "※範囲:", $limitSep, $val[1]);
                    }
                    if (in_array($key, array("S_MONTH","E_MONTH")) && $data[$key] != "" && ($data[$key] < 1 || $data[$key] > 12)) {
                        $returnFlg = $this->errSet($limitErrFlg, $limitErrMsg, "※範囲:", $limitSep, $val[1]);
                    }
                }
            }

            if ($this->field["SHORI_MEI"] == "1") {
                //範囲チェック
                if ((strlen(trim($data["S_YEAR"])) && strlen(trim($data["S_MONTH"])) && strlen(trim($data["E_YEAR"])) && strlen(trim($data["E_MONTH"]))) &&
                    $data["S_YEAR"].sprintf("%02d", $data["S_MONTH"]) > $data["E_YEAR"].sprintf("%02d", $data["E_MONTH"])) {
                    $returnFlg = $this->errSet($limitErrFlg, $limitErrMsg, "※範囲:", $limitSep, "期間");
                }
            }

            //存在チェック
            $table = ($this->field["DATA_KIND"] == "1") ? "before" : "after";
            if ($data["SEQ"] != "" && !in_array($data["SEQ"], $db->getCol(knjf164Query::getSickRecSEQ($data["SCHREGNO"], $table, "list")))) {
                $returnFlg = $this->errSet($sonzaiErrFlg, $sonzaiErrMsg, "※存在:", $sonzaiSep, $data_each["SEQ"][1]);
            }
            if ($this->field["DATA_KIND"] == "1") {
                if ($this->field["SHORI_MEI"] == "1" && !strlen(trim($data["DISEASE"]).trim($data["S_YEAR"]).trim($data["S_MONTH"]).trim($data["E_YEAR"])
                        .trim($data["E_MONTH"]).trim($data["SITUATION"]))) {
                    $returnFlg = $this->errSet($sonzaiErrFlg, $sonzaiErrMsg, "※存在:", $sonzaiSep, "データなし");
                }
            } else {
                if ($this->field["SHORI_MEI"] == "1" && !strlen(trim($data["DISEASE"]).trim($data["S_YEAR"]).trim($data["S_MONTH"]).trim($data["E_YEAR"]).trim($data["E_MONTH"])
                        .trim($data["HOSPITAL"]).trim($data["DOCTOR"]).trim($data["TELNO"]).trim($data["MEDICINE"]).trim($data["SITUATION"]).trim($data["SUSPECT"]))) {
                    $returnFlg = $this->errSet($sonzaiErrFlg, $sonzaiErrMsg, "※存在:", $sonzaiSep, "データなし");
                }
            }
        } elseif ($this->field["DATA_KIND"] == "3") {
            foreach ($data_each as $key => $val) {
                //必須項目チェック
                if ($this->field["SHORI_MEI"] == "1") {
                    if (in_array($key, array("SCHREGNO","RELANAME"))) {
                        if (!strlen(trim($data[$key]))) {
                            $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                        }
                    }
                } else {
                    if (in_array($key, array("SCHREGNO","RELANO"))) {
                        if (!strlen(trim($data[$key]))) {
                            $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                        }
                    }
                }

                //数値チェック
                if (in_array($key, array("SCHREGNO","RELANO","RELATIONSHIP"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }

                if ($this->field["SHORI_MEI"] == "1") {
                    //文字数チェック
                    if (strlen($data[$key]) > $val[0]) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                }
            }

            if ($this->field["SHORI_MEI"] == "1") {
                //範囲チェック
                $birthday = explode("-", str_replace("/", "-", $data["RELABIRTHDAY"]));
                if ($data["RELABIRTHDAY"] != "" && (($birthday[0] < 1900 || $birthday[0] > 2100) || ($birthday[1] < 1 || $birthday[1] > 12) || ($birthday[2] < 1 || $birthday[2] > 31))) {
                    $returnFlg = $this->errSet($limitErrFlg, $limitErrMsg, "※範囲:", $limitSep, $data_each["RELABIRTHDAY"][1]);
                }
            }

            //存在チェック
            if ($data["RELANO"] != "" && !in_array($data["RELANO"], $db->getCol(knjf164Query::getRelaNo($data["SCHREGNO"], "schreg", "list")), true)) {
                $returnFlg = $this->errSet($sonzaiErrFlg, $sonzaiErrMsg, "※存在:", $sonzaiSep, $data_each["RELANO"][1]);
            }
            if ($this->field["SHORI_MEI"] == "1" && !strlen(trim($data["RELANAME"]).trim($data["RELAKANA"]).trim($data["RELABIRTHDAY"]).trim($data["RELATIONSHIP"]).trim($data["REMARK"]))) {
                $returnFlg = $this->errSet($sonzaiErrFlg, $sonzaiErrMsg, "※存在:", $sonzaiSep, "データなし");
            }
        } elseif ($this->field["DATA_KIND"] == "4") {
            foreach ($data_each as $key => $val) {
                //必須項目チェック
                if (in_array($key, array("SCHREGNO"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }
                //数値チェック
                if ($val[2] == "1") {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }

                if ($this->field["SHORI_MEI"] == "1") {
                    //文字数チェック
                    if (strlen($data[$key]) > $val[0]) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                }
            }

            if ($this->field["SHORI_MEI"] == "1") {
                //範囲チェック
                if ($data["BLOOD"] != "" && !in_array($data["BLOOD"], array("A","B","O","AB"))) {
                    $returnFlg = $this->errSet($limitErrFlg, $limitErrMsg, "※範囲:", $limitSep, $data_each["BLOOD"][1]);
                }
            }
        } elseif ($this->field["DATA_KIND"] == "5_1") {
            //数値チェック対象
            $num_check = array();
            $num_check = array("SCHREGNO","INVEST_YEAR","E_YEAR","E_MONTH");
            $result = $db->query(knjf164Query::getHealthQuestion());
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $num_check[] = "ANSWER".$row["QUESTIONCD"];
            }
            $result->free();

            foreach ($data_each as $key => $val) {
                //必須項目チェック
                if (in_array($key, array("SCHREGNO","INVEST_YEAR"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }

                //数値チェック
                if (in_array($key, $num_check)) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }

                if ($this->field["SHORI_MEI"] == "1") {
                    //文字数チェック
                    if (strlen($data[$key]) > $val[0]) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }

                    //範囲チェック
                    if (in_array($key, array("E_YEAR")) && $data[$key] != "" && ($data[$key] < 1900 || $data[$key] > 2100)) {
                        $returnFlg = $this->errSet($limitErrFlg, $limitErrMsg, "※範囲:", $limitSep, $val[1]);
                    }
                    if (in_array($key, array("E_MONTH")) && $data[$key] != "" && ($data[$key] < 1 || $data[$key] > 12)) {
                        $returnFlg = $this->errSet($limitErrFlg, $limitErrMsg, "※範囲:", $limitSep, $val[1]);
                    }
                }
            }

            //存在チェック
            if ("0" == get_count($db->getCol($query = knjf164Query::getHealthQuestion()))) {
                $returnFlg = $this->errSet($sonzaiErrFlg, $sonzaiErrMsg, "※存在:", $sonzaiSep, "健康調査質問マスタ");
            }
        } elseif ($this->field["DATA_KIND"] == "5_2") {
            foreach ($data_each as $key => $val) {
                //必須項目チェック
                if (in_array($key, array("SCHREGNO"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }

                //数値チェック
                if (in_array($key, array("SCHREGNO"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }

                if ($this->field["SHORI_MEI"] == "1") {
                    //文字数チェック
                    if (strlen($data[$key]) > $val[0]) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                }
            }

            //存在チェック
            if ($this->field["SHORI_MEI"] == "1" && !strlen(trim($data["ATTENTION"]))) {
                $returnFlg = $this->errSet($sonzaiErrFlg, $sonzaiErrMsg, "※存在:", $sonzaiSep, "データなし");
            }
        } elseif ($this->field["DATA_KIND"] == "6") {
            foreach ($data_each as $key => $val) {
                //必須項目チェック
                if (in_array($key, array("SCHREGNO"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }

                //数値チェック
                if (in_array($key, array("SCHREGNO","RELATIONSHIP"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }

                if ($this->field["SHORI_MEI"] == "1") {
                    //文字数チェック
                    if (strlen($data[$key]) > $val[0]) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                }
            }

            if ($this->field["SHORI_MEI"] == "1") {
                //範囲チェック
                $valid = explode("-", str_replace("/", "-", $data["VALID_DATE"]));
                if ($data["VALID_DATE"] != "" && (($valid[0] < 1900 || $valid[0] > 2100) || ($valid[1] < 1 || $valid[1] > 12) || ($valid[2] < 1 || $valid[2] > 31))) {
                    $returnFlg = $this->errSet($limitErrFlg, $limitErrMsg, "※範囲:", $limitSep, $data_each["VALID_DATE"][1]);
                }
                $authorize = explode("-", str_replace("/", "-", $data["AUTHORIZE_DATE"]));
                if ($data["AUTHORIZE_DATE"] != "" && (($authorize[0] < 1900 || $authorize[0] > 2100) || ($authorize[1] < 1 || $authorize[1] > 12) || ($authorize[2] < 1 || $authorize[2] > 31))) {
                    $returnFlg = $this->errSet($limitErrFlg, $limitErrMsg, "※範囲:", $limitSep, $data_each["AUTHORIZE_DATE"][1]);
                }
            }

            //存在チェック
            if ($this->field["SHORI_MEI"] == "1" && !strlen(trim($data["INSURED_NAME"]).trim($data["INSURED_MARK"]).trim($data["INSURED_NO"]).trim($data["INSURANCE_NAME"])
                    .trim($data["INSURANCE_NO"]).trim($data["VALID_DATE"]).trim($data["AUTHORIZE_DATE"]).trim($data["RELATIONSHIP"]).trim($data["REMARK"]).trim($data["ATTENTION"]))) {
                $returnFlg = $this->errSet($sonzaiErrFlg, $sonzaiErrMsg, "※存在:", $sonzaiSep, "データなし");
            }
        }
        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$limitErrMsg.$sonzaiErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjf164Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //データの整合性チェック
    public function isDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;
    }

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        //ヘッダデータ
        if ($this->field["DATA_KIND"] == "1") {
            $header = array("YEAR"              => "年度",
                            "GRADE"             => "学年",
                            "HR_CLASS"          => "クラス",
                            "ATTENDNO"          => "出席番号",
                            "SCHREGNO"          => "※学籍番号",
                            "NAME"              => "氏名",
                            "SEQ"               => "連番",
                            "DISEASE"           => "病名",
                            "S_YEAR"            => "期間（開始年）",
                            "S_MONTH"           => "期間（開始月）",
                            "E_YEAR"            => "期間（終了年）",
                            "E_MONTH"           => "期間（終了月）",
                            "SITUATION"         => "経過状況",
                            $this->lastColumn   => $this->lastColumn);

            $header_rei = array("YEAR"            => "9999",
                                "GRADE"           => "99",
                                "HR_CLASS"        => "999",
                                "ATTENDNO"        => "999",
                                "SCHREGNO"        => "12345678",
                                "NAME"            => "アルプ太郎",
                                "SEQ"             => "99",
                                "DISEASE"         => "全角20文字",
                                "S_YEAR"          => "9999",
                                "S_MONTH"         => "99",
                                "E_YEAR"          => "9999",
                                "E_MONTH"         => "99",
                                "SITUATION"       => "全角40文字",
                                $this->lastColumn => $this->lastColumn);
        } elseif ($this->field["DATA_KIND"] == "2") {
            $header = array("YEAR"              => "年度",
                            "GRADE"             => "学年",
                            "HR_CLASS"          => "クラス",
                            "ATTENDNO"          => "出席番号",
                            "SCHREGNO"          => "※学籍番号",
                            "NAME"              => "氏名",
                            "SEQ"               => "連番",
                            "DISEASE"           => "病名",
                            "S_YEAR"            => "期間（開始年）",
                            "S_MONTH"           => "期間（開始月）",
                            "E_YEAR"            => "期間（終了年）",
                            "E_MONTH"           => "期間（終了月）",
                            "SUSPECT"           => "出席停止（1:あり）",
                            "HOSPITAL"          => "病院名",
                            "DOCTOR"            => "主治医",
                            "TELNO"             => "電話番号",
                            "MEDICINE"          => "服用薬",
                            "SITUATION"         => "経過状況",
                            $this->lastColumn   => $this->lastColumn);

            $header_rei = array("YEAR"            => "9999",
                                "GRADE"           => "99",
                                "HR_CLASS"        => "999",
                                "ATTENDNO"        => "999",
                                "SCHREGNO"        => "12345678",
                                "NAME"            => "アルプ太郎",
                                "SEQ"             => "99",
                                "DISEASE"         => "全角20文字",
                                "S_YEAR"          => "9999",
                                "S_MONTH"         => "99",
                                "E_YEAR"          => "9999",
                                "E_MONTH"         => "99",
                                "SUSPECT"         => "1",
                                "HOSPITAL"        => "全角20文字",
                                "DOCTOR"          => "全角20文字",
                                "TELNO"           => "03-1234-5678",
                                "MEDICINE"        => "全角20文字",
                                "SITUATION"       => "全角40文字",
                                $this->lastColumn => $this->lastColumn);
        } elseif ($this->field["DATA_KIND"] == "3") {
            $header = array("YEAR"              => "年度",
                            "GRADE"             => "学年",
                            "HR_CLASS"          => "クラス",
                            "ATTENDNO"          => "出席番号",
                            "SCHREGNO"          => "※学籍番号",
                            "NAME"              => "氏名",
                            "RELANO"            => "親族番号",
                            "RELANAME"          => "※親族氏名",
                            "RELAKANA"          => "親族氏名かな",
                            "RELATIONSHIP"      => "親族続柄",
                            "RELABIRTHDAY"      => "親族生年月日",
                            "REMARK"            => "健康状態（備考）",
                            $this->lastColumn   => $this->lastColumn);

            $header_rei = array("YEAR"            => "9999",
                                "GRADE"           => "99",
                                "HR_CLASS"        => "999",
                                "ATTENDNO"        => "999",
                                "SCHREGNO"        => "12345678",
                                "NAME"            => "アルプ太郎",
                                "RELANO"          => "99",
                                "RELANAME"        => "全角20文字",
                                "RELAKANA"        => "全角40文字",
                                "RELATIONSHIP"    => "99",
                                "RELABIRTHDAY"    => "9999-99-99",
                                "REMARK"          => "全角30文字",
                                $this->lastColumn => $this->lastColumn);
        } elseif ($this->field["DATA_KIND"] == "4") {
            $header = array("YEAR"                      => "年度",
                            "GRADE"                     => "学年",
                            "HR_CLASS"                  => "クラス",
                            "ATTENDNO"                  => "出席番号",
                            "SCHREGNO"                  => "※学籍番号",
                            "NAME"                      => "氏名",
                            "ALLERGY_MEDICINE"          => "薬アレルギー",
                            "ALLERGY_FOOD"              => "食品アレルギー",
                            "ALLERGY_OTHER"             => "その他アレルギー",
                            "BLOOD"                     => "血液型（A/B/O/AB）",
                            "RH"                        => "Ｒｈ",
                            "MEASLES_AGE"               => "麻疹（年齢）",
                            "G_MEASLES_AGE"             => "風疹（年齢）",
                            "VARICELLA_AGE"             => "水痘（年齢）",
                            "OTITIS_MEDIA_AGE"          => "中耳炎（年齢）",
                            "TB_AGE"                    => "結核（年齢）",
                            "KAWASAKI_AGE"              => "川崎病（年齢）",
                            "INFECTION_AGE"             => "溶連菌感染症（年齢）",
                            "MUMPS_AGE"                 => "流行性耳下腺炎（年齢）",
                            "HEART_DISEASE"             => "心臓疾患（病名）",
                            "HEART_S_AGE"               => "心臓疾患（年齢）",
                            "HEART_SITUATION"           => "心臓疾患（経過）",
                            "HEART_E_AGE"               => "心臓疾患（全治年齢）",
                            "KIDNEY_DISEASE"            => "腎臓疾患（病名）",
                            "KIDNEY_S_AGE"              => "腎臓疾患（年齢）",
                            "KIDNEY_SITUATION"          => "腎臓疾患（経過）",
                            "KIDNEY_E_AGE"              => "腎臓疾患（全治年齢）",
                            "ASTHMA_S_AGE"              => "ぜんそく（年齢）",
                            "ASTHMA_SITUATION"          => "ぜんそく（経過）",
                            "ASTHMA_E_AGE"              => "ぜんそく（全治年齢）",
                            "CONVULSIONS_S_AGE"         => "けいれん（年齢）",
                            "CONVULSIONS_SITUATION"     => "けいれん（経過）",
                            "CONVULSIONS_E_AGE"         => "けいれん（全治年齢）",
                            "OTHER_DISEASE"             => "その他（病名）",
                            "TUBERCULIN"                => "ツベルクリン（接種）",
                            "TUBERCULIN_YEAR"           => "ツベルクリン（年）",
                            "TUBERCULIN_MONTH"          => "ツベルクリン（月）",
                            "TUBERCULIN_JUDGE"          => "ツベルクリン（判定）",
                            "BCG"                       => "ＢＣＧ（接種）",
                            "BCG_YEAR"                  => "ＢＣＧ（年）",
                            "BCG_MONTH"                 => "ＢＣＧ（月）",
                            "POLIO"                     => "ポリオ（接種）",
                            "POLIO_YEAR"                => "ポリオ（年）",
                            "POLIO_MONTH"               => "ポリオ（月）",
                            "G_MEASLES"                 => "風疹（接種）",
                            "G_MEASLES_YEAR"            => "風疹（年）",
                            "G_MEASLES_MONTH"           => "風疹（月）",
                            "VARICELLA"                 => "水痘（接種）",
                            "VARICELLA_YEAR"            => "水痘（年）",
                            "VARICELLA_MONTH"           => "水痘（月）",
                            "MUMPS"                     => "流行性耳下腺炎（接種）",
                            "MUMPS_YEAR"                => "流行性耳下腺炎（年）",
                            "MUMPS_MONTH"               => "流行性耳下腺炎（月）",
                            "ENCEPHALITIS"              => "日本脳炎（接種）",
                            "ENCEPHALITIS_YEAR1"        => "日本脳炎（１回・年）",
                            "ENCEPHALITIS_MONTH1"       => "日本脳炎（１回・月）",
                            "ENCEPHALITIS_YEAR2"        => "日本脳炎（２回・年）",
                            "ENCEPHALITIS_MONTH2"       => "日本脳炎（２回・月）",
                            "ENCEPHALITIS_YEAR3"        => "日本脳炎（３回・年）",
                            "ENCEPHALITIS_MONTH3"       => "日本脳炎（３回・月）",
                            "ENCEPHALITIS_YEAR4"        => "日本脳炎（小４・年）",
                            "ENCEPHALITIS_MONTH4"       => "日本脳炎（小４・月）",
                            "ENCEPHALITIS_YEAR5"        => "日本脳炎（中２・年）",
                            "ENCEPHALITIS_MONTH5"       => "日本脳炎（中２・月）",
                            "MIXED"                     => "二種混合（接種）",
                            "MIXED_YEAR1"               => "二種混合（１回・年）",
                            "MIXED_MONTH1"              => "二種混合（１回・月）",
                            "MIXED_YEAR2"               => "二種混合（２回・年）",
                            "MIXED_MONTH2"              => "二種混合（２回・月）",
                            "MIXED_YEAR3"               => "二種混合（３回・年）",
                            "MIXED_MONTH3"              => "二種混合（３回・月）",
                            "MIXED_YEAR4"               => "二種混合（追加・年）",
                            "MIXED_MONTH4"              => "二種混合（追加・月）",
                            "MIXED_YEAR5"               => "二種混合（小６・年）",
                            "MIXED_MONTH5"              => "二種混合（小６・月）",
                            "MEASLES"                   => "麻疹（接種）（1:ある 2:ない）",
                            "MEASLES_TIMES"             => "麻疹（回数）",
                            "MEASLES_YEAR1"             => "麻疹（１回・年）",
                            "MEASLES_MONTH1"            => "麻疹（１回・月）",
                            "MEASLES_YEAR2"             => "麻疹（２回・年）",
                            "MEASLES_MONTH2"            => "麻疹（２回・月）",
                            "MEASLES_YEAR3"             => "麻疹（３回・年）",
                            "MEASLES_MONTH3"            => "麻疹（３回・月）",
                            "VACCINE"                   => "ワクチンの種類",
                            "LOT_NO"                    => "ロット番号",
                            "CONFIRMATION"              => "確認方法",
                            "A_MEASLES"                 => "麻疹（罹患歴）（1:ある 2:ない）",
                            "A_MEASLES_AGE"             => "麻疹（罹患歴・年齢）",
                            "A_CONFIRMATION"            => "確認方法（罹患歴）",
                            "ANTIBODY"                  => "抗体検査（1:ある 2:ない）",
                            "ANTIBODY_YEAR"             => "検査日（年）",
                            "ANTIBODY_MONTH"            => "検査日（月）",
                            "ANTIBODY_POSITIVE"         => "抗体有無（1:抗体ある 2:抗体なし）",
                            $this->lastColumn           => $this->lastColumn);

            $header_rei = array("YEAR"                  => "9999",
                                "GRADE"                 => "99",
                                "HR_CLASS"              => "999",
                                "ATTENDNO"              => "999",
                                "SCHREGNO"              => "12345678",
                                "NAME"                  => "アルプ太郎",
                                "ALLERGY_MEDICINE"      => "全角40文字",
                                "ALLERGY_FOOD"          => "全角40文字",
                                "ALLERGY_OTHER"         => "全角40文字",
                                "BLOOD"                 => "AB",
                                "RH"                    => "+",
                                "MEASLES_AGE"           => "99",
                                "G_MEASLES_AGE"         => "99",
                                "VARICELLA_AGE"         => "99",
                                "OTITIS_MEDIA_AGE"      => "99",
                                "TB_AGE"                => "99",
                                "KAWASAKI_AGE"          => "99",
                                "INFECTION_AGE"         => "99",
                                "MUMPS_AGE"             => "99",
                                "HEART_DISEASE"         => "全角15文字",
                                "HEART_S_AGE"           => "99",
                                "HEART_SITUATION"       => "99",
                                "HEART_E_AGE"           => "99",
                                "KIDNEY_DISEASE"        => "全角15文字",
                                "KIDNEY_S_AGE"          => "99",
                                "KIDNEY_SITUATION"      => "99",
                                "KIDNEY_E_AGE"          => "99",
                                "ASTHMA_S_AGE"          => "99",
                                "ASTHMA_SITUATION"      => "99",
                                "ASTHMA_E_AGE"          => "99",
                                "CONVULSIONS_S_AGE"     => "99",
                                "CONVULSIONS_SITUATION" => "99",
                                "CONVULSIONS_E_AGE"     => "99",
                                "OTHER_DISEASE"         => "全角40文字",
                                "TUBERCULIN"            => "99",
                                "TUBERCULIN_YEAR"       => "9999",
                                "TUBERCULIN_MONTH"      => "99",
                                "TUBERCULIN_JUDGE"      => "9",
                                "BCG"                   => "99",
                                "BCG_YEAR"              => "9999",
                                "BCG_MONTH"             => "99",
                                "POLIO"                 => "99",
                                "POLIO_YEAR"            => "9999",
                                "POLIO_MONTH"           => "99",
                                "G_MEASLES"             => "99",
                                "G_MEASLES_YEAR"        => "9999",
                                "G_MEASLES_MONTH"       => "99",
                                "VARICELLA"             => "99",
                                "VARICELLA_YEAR"        => "9999",
                                "VARICELLA_MONTH"       => "99",
                                "MUMPS"                 => "99",
                                "MUMPS_YEAR"            => "9999",
                                "MUMPS_MONTH"           => "99",
                                "ENCEPHALITIS"          => "99",
                                "ENCEPHALITIS_YEAR1"    => "9999",
                                "ENCEPHALITIS_MONTH1"   => "99",
                                "ENCEPHALITIS_YEAR2"    => "9999",
                                "ENCEPHALITIS_MONTH2"   => "99",
                                "ENCEPHALITIS_YEAR3"    => "9999",
                                "ENCEPHALITIS_MONTH3"   => "99",
                                "ENCEPHALITIS_YEAR4"    => "9999",
                                "ENCEPHALITIS_MONTH4"   => "99",
                                "ENCEPHALITIS_YEAR5"    => "9999",
                                "ENCEPHALITIS_MONTH5"   => "99",
                                "MIXED"                 => "99",
                                "MIXED_YEAR1"           => "9999",
                                "MIXED_MONTH1"          => "99",
                                "MIXED_YEAR2"           => "9999",
                                "MIXED_MONTH2"          => "99",
                                "MIXED_YEAR3"           => "9999",
                                "MIXED_MONTH3"          => "99",
                                "MIXED_YEAR4"           => "9999",
                                "MIXED_MONTH4"          => "99",
                                "MIXED_YEAR5"           => "9999",
                                "MIXED_MONTH5"          => "99",
                                "MEASLES"               => "9",
                                "MEASLES_TIMES"         => "99",
                                "MEASLES_YEAR1"         => "9999",
                                "MEASLES_MONTH1"        => "99",
                                "MEASLES_YEAR2"         => "9999",
                                "MEASLES_MONTH2"        => "99",
                                "MEASLES_YEAR3"         => "9999",
                                "MEASLES_MONTH3"        => "99",
                                "VACCINE"               => "99",
                                "LOT_NO"                => "9999999999",
                                "CONFIRMATION"          => "99",
                                "A_MEASLES"             => "9",
                                "A_MEASLES_AGE"         => "99",
                                "A_CONFIRMATION"        => "99",
                                "ANTIBODY"              => "9",
                                "ANTIBODY_YEAR"         => "9999",
                                "ANTIBODY_MONTH"        => "99",
                                "ANTIBODY_POSITIVE"     => "1",
                                $this->lastColumn       => $this->lastColumn);
        } elseif ($this->field["DATA_KIND"] == "5_1") {
            $header = $header_rei = array();

            $header = array("YEAR"              => "年度",
                            "GRADE"             => "学年",
                            "HR_CLASS"          => "クラス",
                            "ATTENDNO"          => "出席番号",
                            "SCHREGNO"          => "※学籍番号",
                            "NAME"              => "氏名",
                            "INVEST_YEAR"       => "※調査年度",
                            "E_YEAR"            => "記入日（年）",
                            "E_MONTH"           => "記入日（月）",
                            "QUESTIONCD"        => "質問コード",);


            $header_rei = array("YEAR"          => "9999",
                                "GRADE"         => "99",
                                "HR_CLASS"      => "999",
                                "ATTENDNO"      => "999",
                                "SCHREGNO"      => "12345678",
                                "NAME"          => "アルプ太郎",
                                "INVEST_YEAR"   => "9999",
                                "E_YEAR"        => "9999",
                                "E_MONTH"       => "99");

            $query = knjf164Query::getHealthQuestion();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $header["QUESTIONCD".$row["QUESTIONCD"]]     =  $row["CONTENTS"];
                $header_rei["QUESTIONCD".$row["QUESTIONCD"]] =  "（1:ある 2:ときどきある 3:ない）";
            }
            $result->free();

            $header[$this->lastColumn]     = $this->lastColumn;
            $header_rei[$this->lastColumn] = $this->lastColumn;
        } elseif ($this->field["DATA_KIND"] == "5_2") {
            $header = array("YEAR"              => "年度",
                            "GRADE"             => "学年",
                            "HR_CLASS"          => "クラス",
                            "ATTENDNO"          => "出席番号",
                            "SCHREGNO"          => "※学籍番号",
                            "NAME"              => "氏名",
                            "ATTENTION"         => "注意すべき点",
                            $this->lastColumn   => $this->lastColumn);

            $header_rei = array("YEAR"            => "9999",
                                "GRADE"           => "99",
                                "HR_CLASS"        => "999",
                                "ATTENDNO"        => "999",
                                "SCHREGNO"        => "12345678",
                                "NAME"            => "アルプ太郎",
                                "ATTENTION"       => "全角200文字",
                                $this->lastColumn => $this->lastColumn);
        } elseif ($this->field["DATA_KIND"] == "6") {
            $header = array("YEAR"                  => "年度",
                            "GRADE"                 => "学年",
                            "HR_CLASS"              => "クラス",
                            "ATTENDNO"              => "出席番号",
                            "SCHREGNO"              => "※学籍番号",
                            "NAME"                  => "氏名",
                            "INSURED_NAME"          => "被保険者氏名",
                            "INSURED_MARK"          => "加入者記号",
                            "INSURED_NO"            => "加入者番号",
                            "INSURANCE_NAME"        => "保険者名称",
                            "INSURANCE_NO"          => "保険者番号",
                            "VALID_DATE"            => "有効期限",
                            "AUTHORIZE_DATE"        => "被扶養者認定年月",
                            "RELATIONSHIP"          => "続柄",
                            "REMARK"                => "保健室記入用備考",
                            "ATTENTION"             => "山上・海浜学校のための注意点",
                            $this->lastColumn       => $this->lastColumn);

            $header_rei = array("YEAR"              => "9999",
                                "GRADE"             => "99",
                                "HR_CLASS"          => "999",
                                "ATTENDNO"          => "999",
                                "SCHREGNO"          => "12345678",
                                "NAME"              => "アルプ太郎",
                                "INSURED_NAME"      => "全角20文字",
                                "INSURED_MARK"      => "60バイト",
                                "INSURED_NO"        => "20バイト",
                                "INSURANCE_NAME"    => "全角20文字",
                                "INSURANCE_NO"      => "20バイト",
                                "VALID_DATE"        => "9999-99-99",
                                "AUTHORIZE_DATE"    => "9999-99-99",
                                "RELATIONSHIP"      => "99",
                                "REMARK"            => "全角400文字",
                                "ATTENTION"         => "全角30文字",
                                $this->lastColumn   => $this->lastColumn);
        }

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"] == "1") {
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "3") {
            //エラー
            $tables = array();
            if ($this->field["DATA_KIND"] == "3") {
                $tables = array("1" => "SCHREG_RELA_DAT"
                                , "2" => "HEALTH_RELA_DAT"
                );
            } elseif ($this->field["DATA_KIND"] == "4") {
                $tables = array("1" => "HEALTH_INVEST_OTHER_DAT");
            } elseif ($this->field["DATA_KIND"] == "5_1") {
                $tables = array("1" => "HEALTH_INVEST_DAT");
            } elseif ($this->field["DATA_KIND"] == "5_2") {
                $tables = array("1" => "HEALTH_INVEST_ATTENTION_DAT");
            } elseif ($this->field["DATA_KIND"] == "6") {
                $tables = array("1" => "HEALTH_NURSE_ENT_DAT");
            }
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $header);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($header_err, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "4") {
            //データ
            if ($this->field["HEADER"] == "on") {
                $contents .= implode($header, ",")."\n";
            }
        }

        //DB接続
        $db = Query::dbCheckOut();

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") {
            if ($this->field["OUTPUT"] == "3") {
                //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"] == "4") {
                //データ
                if ($this->field["DATA_KIND"] == "1") {
                    $query = knjf164Query::selectMainQuery1($this);
                } elseif ($this->field["DATA_KIND"] == "2") {
                    $query = knjf164Query::selectMainQuery2($this);
                } elseif ($this->field["DATA_KIND"] == "3") {
                    $query = knjf164Query::selectMainQuery3($this);
                } elseif ($this->field["DATA_KIND"] == "4") {
                    $query = knjf164Query::selectMainQuery4($this);
                } elseif ($this->field["DATA_KIND"] == "5_1") {
                    $query = knjf164Query::selectMainQuery5_1($this);
                } elseif ($this->field["DATA_KIND"] == "5_2") {
                    $query = knjf164Query::selectMainQuery5_2($this);
                } elseif ($this->field["DATA_KIND"] == "6") {
                    $query = knjf164Query::selectMainQuery6($this);
                }
            }

            if ($this->field["OUTPUT"] == "4" && $this->field["DATA_KIND"] == "5_1") {
                $dataTemp = array();
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $row = str_replace(",", ".", $row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    if ($dataTemp[$row["SCHREGNO"].$row["INVEST_YEAR"]] == "") {
                        $dataTemp[$row["SCHREGNO"].$row["INVEST_YEAR"]][] = $row["YEAR"];
                        $dataTemp[$row["SCHREGNO"].$row["INVEST_YEAR"]][] = $row["GRADE"];
                        $dataTemp[$row["SCHREGNO"].$row["INVEST_YEAR"]][] = $row["HR_CLASS"];
                        $dataTemp[$row["SCHREGNO"].$row["INVEST_YEAR"]][] = $row["ATTENDNO"];
                        $dataTemp[$row["SCHREGNO"].$row["INVEST_YEAR"]][] = $row["SCHREGNO"];
                        $dataTemp[$row["SCHREGNO"].$row["INVEST_YEAR"]][] = $row["NAME"];
                        $dataTemp[$row["SCHREGNO"].$row["INVEST_YEAR"]][] = $row["INVEST_YEAR"];
                        $dataTemp[$row["SCHREGNO"].$row["INVEST_YEAR"]][] = $row["E_YEAR"];
                        $dataTemp[$row["SCHREGNO"].$row["INVEST_YEAR"]][] = $row["E_MONTH"];
                        $dataTemp[$row["SCHREGNO"].$row["INVEST_YEAR"]][] = $row["ANSWER"];
                    } else {
                        $dataTemp[$row["SCHREGNO"].$row["INVEST_YEAR"]][] = $row["ANSWER"];
                    }
                }

                $retsu_cnt = get_count($db->getCol(knjf164Query::getHealthQuestion()))+9;
                foreach ($dataTemp as $key => $val) {
                    $contents .= implode($val, ",");
                    if ($retsu_cnt !=get_count($val)) {
                        for ($i = get_count($val); $i < $retsu_cnt; $i++) {
                            $contents .= ",";
                        }
                    }
                    $contents .= ",".$this->lastColumn."\n";
                    $header_data = true;
                }
            } else {
                //SQL文発行
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($this->field["OUTPUT"] == "4") {              //データ
                        $row = str_replace(",", ".", $row);     //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                        $row = str_replace("\r\n", "↓", $row);
                        $row = str_replace("\r", "↓", $row);
                        $row = str_replace("\n", "↓", $row);
                    }
                    $contents .= implode($row, ",") ."\n";
                    $header_data = true;
                }
            }
            $result->free();
        }

        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "3") {      //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                if ($this->field["DATA_KIND"] == "1") {
                    $subtitle = "病気けが（入学前）";
                } elseif ($this->field["DATA_KIND"] == "2") {
                    $subtitle = "病気けが（入学後）";
                } elseif ($this->field["DATA_KIND"] == "3") {
                    $subtitle = "家族情報";
                } elseif ($this->field["DATA_KIND"] == "4") {
                    $subtitle = "その他病気";
                } elseif ($this->field["DATA_KIND"] == "5_1") {
                    $subtitle = "健康調査";
                } elseif ($this->field["DATA_KIND"] == "5_2") {
                    $subtitle = "健康調査（生活上注意すべき点）";
                } elseif ($this->field["DATA_KIND"] == "6") {
                    $subtitle = "保健室記入";
                }
                $couse = "保健調査票記録（".$subtitle."）";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
