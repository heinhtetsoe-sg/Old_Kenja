<?php

require_once('for_php7.php');


/**
 * 定数定義
*/

class knjm837wModel extends Model
{
    public $field      = array();
    public $cmd;
    public $control;        //コントロールマスタ
    public $staffcd;        //職員コード

    /**
     * メンバ変数の初期化
     */
    public function knjm837wModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //職員コード
    }

    public function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),              //年度
                             "GAKKI"            =>  VARS::post("GAKKI"),             //学期
                             "DBNAME"           =>  VARS::post("DBNAME")             //DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
    }

    //CSVメイン
    public function getDownloadModel()
    {
        //変数定義
        $comma = ","; //カンマ
        $lf = "\n";   //改行
        //DB接続
        $db = Query::dbCheckOut();

        //年度、日付の和暦表示設定
        $ctrlYearWareki = common::DateConv1(CTRL_YEAR."/04/01", 10);
        $ctrlDateWareki  = common::DateConv1(str_replace("-", "/", CTRL_DATE), 0);
        //学期
        $query = knjm837wQuery::getSemeMst(CTRL_YEAR, $this->field["GAKKI"]);
        $semeMst = $db->getRow($query, DB_FETCHMODE_ASSOC);
        //テスト種別
        $query = knjm837wQuery::getTestcd($this, CTRL_YEAR, $this->field["GAKKI"], $this->field["TESTCD"]);
        $testMst = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //CSVデータ取得・・・入学年度毎
        $dataContents = "";
        //入学年度
        $sYear = CTRL_YEAR;
        $eYear = CTRL_YEAR - 3;
        for ($entYear = $sYear; $entYear >= $eYear; $entYear--) {
//echo $entYear."/";
            $dataContents .= $this->getCsvData($db, $entYear, $eYear);
        }

        //DB切断
        Query::dbCheckIn($db);

        //タイトル
        $title = $ctrlYearWareki."　".$semeMst["LABEL"]."　".$testMst["LABEL"]."　成績会議資料";
        //作成日
        $sakuseibi = $ctrlDateWareki."現在";

        $contents  = "";
        $contents .= $title .$lf;
        $contents .= $sakuseibi .$lf;
        $contents .= $dataContents;

        if (strlen($contents)) {
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    //CSVデータ取得
    public function getCsvData($db, $entYear, $eYear)
    {
        //変数定義
        $comma = ","; //カンマ
        $lf = "\n";   //改行
        //DB接続
        //$db = Query::dbCheckOut();

        //指定年度、日付の和暦表示設定
        $entYearWareki = $entYear."年度";

        /*****************
        ** Ⅰ．受験状況 **
        *****************/

        //ヘッダ配列
        $headerI = array("項目",
                        "一般生",
                        "転編入生",
                        "合計",
                        "備考"
                        );
        //ヘッダ配列を出力変数にセット
        $headerContentsI = implode($headerI, $comma) .$lf;

        //行配列
        $arrIppann = array();
        $arrTenHen = array();
        $arrTotal  = array();
        $arrRemark = array();
        //CSVデータを取得し、まず行配列にセット
        $data = false;
        $query = knjm837wQuery::getCsvQueryI($this, $entYear, $eYear);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //一般生
            if ($row["CNT_DIV"] == '02') {
                $arrIppann[1] = $row["CNT"];
            }
            if ($row["CNT_DIV"] == '04') {
                $arrIppann[2] = $row["CNT"];
            }
            if ($row["CNT_DIV"] == '06') {
                $arrIppann[3] = $row["CNT"];
            }
            if ($row["CNT_DIV"] == '13') {
                $arrIppann[5] = $row["CNT"];
            }
            if ($row["CNT_DIV"] == '16') {
                $arrIppann[6] = $row["CNT"];
            }
            if ($row["CNT_DIV"] == '19') {
                $arrIppann[7] = $row["CNT"];
            }
            if ($row["CNT_DIV"] == '22') {
                $arrIppann[8] = $row["CNT"];
            }
            //転編入生
            if ($row["CNT_DIV"] == '03') {
                $arrTenHen[1] = $row["CNT"];
            }
            if ($row["CNT_DIV"] == '05') {
                $arrTenHen[2] = $row["CNT"];
            }
            if ($row["CNT_DIV"] == '07') {
                $arrTenHen[3] = $row["CNT"];
            }
            if ($row["CNT_DIV"] == '14') {
                $arrTenHen[5] = $row["CNT"];
            }
            if ($row["CNT_DIV"] == '17') {
                $arrTenHen[6] = $row["CNT"];
            }
            if ($row["CNT_DIV"] == '20') {
                $arrTenHen[7] = $row["CNT"];
            }
            if ($row["CNT_DIV"] == '23') {
                $arrTenHen[8] = $row["CNT"];
            }
            //備考
            if ($row["CNT_DIV"] == '08-4') {
                $arrRemark[2][1] = "転入生(" . $row["CNT"] . ")";
            }
            if ($row["CNT_DIV"] == '08-5') {
                $arrRemark[2][2] = "編入生(" . $row["CNT"] . ")";
            }
            if ($row["CNT_DIV"] == '09-3') {
                $arrRemark[3][1] = "転学(" . $row["CNT"] . ")";
            }
            if ($row["CNT_DIV"] == '09-2') {
                $arrRemark[3][2] = "退学(" . $row["CNT"] . ")";
            }
            if ($row["CNT_DIV"] == '09-9') {
                $arrRemark[3][3] = "除籍(" . $row["CNT"] . ")";
            }
            if ($row["CNT_DIV"] == '09-1') {
                $arrRemark[3][4] = "卒業(" . $row["CNT"] . ")";
            }
            if ($row["CNT_DIV"] == '15-4') {
                $arrRemark[5][1] = "転入生(" . $row["CNT"] . ")";
            }
            if ($row["CNT_DIV"] == '15-5') {
                $arrRemark[5][2] = "編入生(" . $row["CNT"] . ")";
            }
            if ($row["CNT_DIV"] == '18-3') {
                $arrRemark[6][1] = "転学(" . $row["CNT"] . ")";
            }
            if ($row["CNT_DIV"] == '18-2') {
                $arrRemark[6][2] = "退学(" . $row["CNT"] . ")";
            }
            if ($row["CNT_DIV"] == '18-9') {
                $arrRemark[6][3] = "除籍(" . $row["CNT"] . ")";
            }
            if ($row["CNT_DIV"] == '18-1') {
                $arrRemark[6][4] = "卒業(" . $row["CNT"] . ")";
            }
            if ($row["CNT_DIV"] == '21-1') {
                $arrRemark[7][1] = "(内 卒業予定 " . $row["CNT"] . " 名)";
            }
            if ($row["CNT_DIV"] == '28-1') {
                $arrRemark[8][1] = "(内 卒業予定 " . $row["CNT"] . " 名)";
            }

            $data = true;
        }
        //本年度始めの在籍者数
        $arrIppann[4] = $arrIppann[1] + $arrIppann[2] - $arrIppann[3];
        $arrTenHen[4] = $arrTenHen[1] + $arrTenHen[2] - $arrTenHen[3];
        //合計
        for ($gyo = 1; $gyo <= 8; $gyo++) {
            $arrTotal[$gyo] = $arrIppann[$gyo] + $arrTenHen[$gyo];
        }
        //受験率
        $arrIppann[9] = ($arrIppann[7] > 0) ? $arrIppann[8] / $arrIppann[7] * 100 : 0.0;
        $arrTenHen[9] = ($arrTenHen[7] > 0) ? $arrTenHen[8] / $arrTenHen[7] * 100 : 0.0;
        $arrTotal[9]  = ( $arrTotal[7] > 0) ?  $arrTotal[8] /  $arrTotal[7] * 100 : 0.0;
        //未受験者数
        $arrIppann[10] = $arrIppann[7] - $arrIppann[8];
        $arrTenHen[10] = $arrTenHen[7] - $arrTenHen[8];
        $arrTotal[10]  =  $arrTotal[7] -  $arrTotal[8];
        //項目
        $arrItem = array("1" => "入学者数",
                         "2" => "前年度までの転入学者数",
                         "3" => "前年度までの転学・退学・除籍・卒業者数",
                         "4" => "本年度始めの在籍者数",
                         "5" => "本年度途中の転入学者数",
                         "6" => "本年度途中の転学・退学・除籍者数",
                         "7" => "試験時の登録者数(Ａ)",
                         "8" => "受験者数(Ｂ)",
                         "9" => "受験率",
                         "10" => "未受験者数"
                        );

        //行配列から出力変数にセット
        $dataContentsI = "";
        foreach ($arrItem as $gyo => $item) {
            $dataContentsI .= $item . $comma;
            $dataContentsI .= $arrIppann[$gyo] . $comma;
            $dataContentsI .= $arrTenHen[$gyo] . $comma;
            $dataContentsI .= $arrTotal[$gyo] . $comma;
            $remark = "";
            if (isset($arrRemark[$gyo])) {
                $seq = "";
                foreach ($arrRemark[$gyo] as $key => $val) {
                    $remark .= $seq . $val;
                    $seq = "、";
                }
            }
            $dataContentsI .= $remark . $comma . $lf;
        }

        /*****************************************************
        ** ３．成績上位者(４科目以上受験して８０点以上の者) **
        *****************************************************/

        //ヘッダ配列
        $header3 = array("学籍番号",
                        "氏名",
                        "平均点",
                        "数",
                        "備考"
                        );
        //ヘッダ配列を出力変数にセット
        $headerContents3 = implode($header3, $comma) .$lf;

        //CSVデータを取得し出力変数にセット
        $dataContents3 = "";
        $query = knjm837wQuery::getCsvQuery34($this, "joui", $entYear, $eYear);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dataContents3 .= $row["SCHREGNO"]       . $comma;
            $dataContents3 .= $row["NAME"]           . $comma;
            $dataContents3 .= $row["AVG"]            . $comma;
            $dataContents3 .= $row["CNT"]            . $comma;
            $dataContents3 .= $row["REMARK"]         . $comma;
            $dataContents3 .= $lf;

            $data = true;
        }

        /***************************************************
        ** ４．欠点保有者(３９点以下の科目をもっている者) **
        ***************************************************/

        //ヘッダ配列
        $header4 = array("学籍番号",
                        "氏名",
                        "平均点",
                        "数",
                        "備考",
                        "K1",
                        "K2",
                        "K3",
                        "K4",
                        "K5",
                        "K6",
                        "K7",
                        "K8",
                        "K9",
                        "K10",
                        "K11",
                        "K12"
                        );
        //ヘッダ配列を出力変数にセット
        $headerContents4 = implode($header4, $comma) .$lf;

        //CSVデータを取得し出力変数にセット
        $dataContents4 = "";
        $query = knjm837wQuery::getCsvQuery34($this, "ketten", $entYear, $eYear);
        $oldSchregno = "";
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($oldSchregno != $row["SCHREGNO"]) {
                if ($oldSchregno) {
                    $dataContents4 .= $lf;
                }
                $dataContents4 .= $row["SCHREGNO"]       . $comma;
                $dataContents4 .= $row["NAME"]           . $comma;
                $dataContents4 .= $row["AVG"]            . $comma;
                $dataContents4 .= $row["CNT"]            . $comma;
                $dataContents4 .= $row["REMARK"]         . $comma;
            }
            if (!strlen($row["SIDOU"])) {
                $row["SIDOU"] = "  ";
            }
            $dataContents4 .= $row["SUBCLASSABBV"] . " " . $row["SCORE"] . "(" . $row["SIDOU"] . ")" . $comma;
            $oldSchregno = $row["SCHREGNO"];

            $data = true;
        }
        $dataContents4 .= $lf;

        //DB切断
        //Query::dbCheckIn($db);

        //CSV出力
        //入学年度生
        $izen = ($entYear == $eYear) ? "以前" : "";
        $nyuugakuSei = $entYearWareki."生".$izen .$lf;
        //サブタイトル
        $subtitleI = "Ⅰ．受験状況" .$lf;
        $subtitle3 = "３．成績上位者(４科目以上受験して８０点以上の者)" .$lf;
        $subtitle4 = "４．欠点保有者(３９点以下の科目をもっている者)" .$lf;

        $contents  = "";
        $contents .= $nyuugakuSei;

        $contents .= $lf;
        $contents .= $subtitleI;
        $contents .= $headerContentsI;
        $contents .= $dataContentsI;

        $contents .= $lf;
        $contents .= $subtitle3;
        $contents .= $headerContents3;
        $contents .= $dataContents3;

        $contents .= $lf;
        $contents .= $subtitle4;
        $contents .= $headerContents4;
        $contents .= $dataContents4;

        return $contents;
    }
}
