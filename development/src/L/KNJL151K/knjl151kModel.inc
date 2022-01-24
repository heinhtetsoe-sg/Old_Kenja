<?php

require_once('for_php7.php');
class knjl151kModel extends Model
{
    public $cmd;
    public $file;
    public $data_arr = array();
    public $controls = array();
    public $headercheck;
    public $testsubclasscd;
    public $testsubclassnm;
    public $coursecd;
    public $majorcd;
    public $coursecode;
    public $wk;
    public $appli;     //出願区分
    public $lastColumn;//CSVの最後の列の値

    public function knjl151kModel()
    {
        $this->year     = CTRL_YEAR + 1;
    }

    public function init()
    {
        $this->field["WHICH_WAY"] = VARS::post("WHICH_WAY");    //更新区分

        $this->cmd            = VARS::request("cmd");
        $temp                 = VARS::post("COURSEMAJOR");
        $this->course         = substr($temp, 0, 1);
        $this->major          = substr($temp, 2, 3);
        $this->testdiv        = VARS::post("TESTDIV");
        $this->testsubclass   = VARS::post("TESTSUBCLASS");
        $this->testsubclasscd = substr($this->testsubclass, 0, 1);
        $this->testsubclassnm = substr($this->testsubclass, 2);

        if (VARS::get("PROGRAMID")) {
            $this->headercheck = "1";
            $this->target      = "01";
        } else {
            $this->headercheck = VARS::post("HEADERCHECK");
            $this->target      = VARS::post("TARGET");
        }

        $this->file = VARS::file("FILE");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    public function &getUpdateModel()
    {
        //事項時間の制限を無くす
        set_time_limit(0);

        $this->data_arr = array();
        //ファイルの文字列の取得
        $data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($data_arr)) {
            $cnt = knjl151kQuery::insertQuery($this, $data_arr);
            $this->setMessage("MSG202", "更新件数：".$cnt."件");
        }
    }

    public function getFileString()
    {
        $temp_arr = array();
        if (is_uploaded_file($this->file["tmp_name"])) {
            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $header = false;
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->headercheck == 1) {
                        $header = true;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);

                    switch ($this->target) {
                        case "1":   //基礎・住所・得点データ
                            $data = array(
                                          "TARGET_TABLE"            => $tmp[0],
                                          "ENTEXAMYEAR"             => $tmp[1],
                                          "TESTDIV"                 => $tmp[2],
                                          "EXAMNO"                  => $tmp[3],
                                          "SHDIV"                   => $tmp[4],
                                          "DESIREDIV"               => $tmp[5],
                                          "APPLICANTDIV"            => $tmp[6],
                                          "CLUBCD"                  => $tmp[7],
                                          "SPECIAL_REASON_DIV"      => $tmp[8],
                                          "NAME"                    => $tmp[9],
                                          "NAME_KANA"               => $tmp[10],
                                          "SEX"                     => $tmp[11],
                                          "BIRTHDAY"                => $tmp[12],
                                          "ADDRESSCD"               => $tmp[13],
                                          "TELNO"                   => $tmp[14],
                                          "LOCATIONCD"              => $tmp[15],
                                          "NATPUBPRIDIV"            => $tmp[16],
                                          "FS_CD"                   => $tmp[17],
                                          "FS_GRDYEAR"              => $tmp[18],
                                          "PS_CD"                   => $tmp[19],
                                          "APPROVAL_FLG"            => $tmp[20],
                                          "GNAME"                   => $tmp[21],
                                          "GKANA"                   => $tmp[22],
                                          "GTELNO"                  => $tmp[23],
                                          "A_TOTAL"                 => $tmp[24],
                                          "A_AVERAGE"               => $tmp[25],
                                          "A_TOTAL_RANK"            => $tmp[26],
                                          "A_DIV_RANK"              => $tmp[27],
                                          "B_TOTAL"                 => $tmp[28],
                                          "B_AVERAGE"               => $tmp[29],
                                          "B_TOTAL_RANK"            => $tmp[30],
                                          "B_DIV_RANK"              => $tmp[31],
                                          "JUDGEMENT"               => $tmp[32],
                                          "JUDGEMENT_GROUP_NO"      => $tmp[33],
                                          "INTERVIEW_ATTEND_FLG"    => $tmp[34],
                                          "SCALASHIPDIV"            => $tmp[35],
                                          "SUC_COURSECD"            => $tmp[36],
                                          "SUC_MAJORCD"             => $tmp[37],
                                          "SUC_COURSECODE"          => $tmp[38],
                                          "PROCEDUREDIV"            => $tmp[39],
                                          "ENTDIV"                  => $tmp[40],
                                          "FORMNO"                  => $tmp[41],
                                          "SUCCESS_NOTICENO"        => $tmp[42],
                                          "OLD_SUCCESS_NOTICENO"    => $tmp[43],
                                          "FAILURE_NOTICENO"        => $tmp[44],
                                          "ZIPCD"                   => $tmp[45],
                                          "ADDRESS"                 => $tmp[46],
                                          "GZIPCD"                  => $tmp[47],
                                          "GADDRESS"                => $tmp[48],
                                          "TESTSUBCLASSCD1"         => $tmp[49],
                                          "ATTEND_FLG1"             => $tmp[50],
                                          "A_SCORE1"                => $tmp[51],
                                          "A_STD_SCORE1"            => $tmp[52],
                                          "A_RANK1"                 => $tmp[53],
                                          "B_SCORE1"                => $tmp[54],
                                          "B_STD_SCORE1"            => $tmp[55],
                                          "B_RANK1"                 => $tmp[56],
                                          "TESTSUBCLASSCD2"         => $tmp[57],
                                          "ATTEND_FLG2"             => $tmp[58],
                                          "A_SCORE2"                => $tmp[59],
                                          "A_STD_SCORE2"            => $tmp[60],
                                          "A_RANK2"                 => $tmp[61],
                                          "B_SCORE2"                => $tmp[62],
                                          "B_STD_SCORE2"            => $tmp[63],
                                          "B_RANK2"                 => $tmp[64],
                                          "TESTSUBCLASSCD3"         => $tmp[65],
                                          "ATTEND_FLG3"             => $tmp[66],
                                          "A_SCORE3"                => $tmp[67],
                                          "A_STD_SCORE3"            => $tmp[68],
                                          "A_RANK3"                 => $tmp[69],
                                          "B_SCORE3"                => $tmp[70],
                                          "B_STD_SCORE3"            => $tmp[71],
                                          "B_RANK3"                 => $tmp[72],
                                          "TESTSUBCLASSCD4"         => $tmp[73],
                                          "ATTEND_FLG4"             => $tmp[74],
                                          "A_SCORE4"                => $tmp[75],
                                          "A_STD_SCORE4"            => $tmp[76],
                                          "A_RANK4"                 => $tmp[77],
                                          "B_SCORE4"                => $tmp[78],
                                          "B_STD_SCORE4"            => $tmp[79],
                                          "B_RANK4"                 => $tmp[80],
                                          "TESTSUBCLASSCD5"         => $tmp[81],
                                          "ATTEND_FLG5"             => $tmp[82],
                                          "A_SCORE5"                => $tmp[83],
                                          "A_STD_SCORE5"            => $tmp[84],
                                          "A_RANK5"                 => $tmp[85],
                                          "B_SCORE5"                => $tmp[86],
                                          "B_STD_SCORE5"            => $tmp[87],
                                          "B_RANK5"                 => $tmp[88],
                                          $this->lastColumn         => $tmp[89]); //LASTCOLUMN OR DUMMY
                            $temp_arr[$i] = $data;
                            break;
                        case "2"://事前相談データ
                            $data = array(
                                          "TARGET_TABLE"    => $tmp[0],
                                          "ENTEXAMYEAR"     => $tmp[1],
                                          "EXAMNO"          => $tmp[2],
                                          "TESTDIV"         => $tmp[3],
                                          "ACCEPTNO"        => $tmp[4],
                                          "CREATE_DATE"     => $tmp[5],
                                          "PS_ACCEPTNO"     => $tmp[6],
                                          "PS_UPDATED"      => $tmp[7],
                                          "PS_CD"           => $tmp[8],
                                          "PS_ITEM1"        => $tmp[9],
                                          "PS_ITEM2"        => $tmp[10],
                                          "PS_ITEM3"        => $tmp[11],
                                          "PS_ITEM4"        => $tmp[12],
                                          "PS_ITEM5"        => $tmp[13],
                                          "PS_ORG_SHDIV1"      => $tmp[14],
                                          "PS_ORG_MAJORCD1"    => $tmp[15],
                                          "PS_ORG_JUDGEMENT1"  => $tmp[16],
                                          "PS_SHDIV1"          => $tmp[17],
                                          "PS_COURSECD1"       => $tmp[18],
                                          "PS_MAJORCD1"        => $tmp[19],
                                          "PS_EXAMCOURSECD1"   => $tmp[20],
                                          "PS_JUDGEMENT1"      => $tmp[21],
                                          "PS_ORG_SHDIV2"      => $tmp[22],
                                          "PS_ORG_MAJORCD2"    => $tmp[23],
                                          "PS_ORG_JUDGEMENT2"  => $tmp[24],
                                          "PS_SHDIV2"          => $tmp[25],
                                          "PS_COURSECD2"       => $tmp[26],
                                          "PS_MAJORCD2"        => $tmp[27],
                                          "PS_EXAMCOURSECD2"   => $tmp[28],
                                          "PS_JUDGEMENT2"      => $tmp[29],
                                          "PS_ORG_SHDIV3"      => $tmp[30],
                                          "PS_ORG_MAJORCD3"    => $tmp[31],
                                          "PS_ORG_JUDGEMENT3"  => $tmp[32],
                                          "PS_SHDIV3"          => $tmp[33],
                                          "PS_COURSECD3"       => $tmp[34],
                                          "PS_MAJORCD3"        => $tmp[35],
                                          "PS_EXAMCOURSECD3"   => $tmp[36],
                                          "PS_JUDGEMENT3"      => $tmp[37],
                                          "PS_ORG_SHDIV4"      => $tmp[38],
                                          "PS_ORG_MAJORCD4"    => $tmp[39],
                                          "PS_ORG_JUDGEMENT4"  => $tmp[40],
                                          "PS_SHDIV4"          => $tmp[41],
                                          "PS_COURSECD4"       => $tmp[42],
                                          "PS_MAJORCD4"        => $tmp[43],
                                          "PS_EXAMCOURSECD4"   => $tmp[44],
                                          "PS_JUDGEMENT4"      => $tmp[45],
                                          "FS_ACCEPTNO"     => $tmp[46],
                                          "FS_UPDATED"      => $tmp[47],
                                          "FS_CD"           => $tmp[48],
                                          "FS_ITEM1"        => $tmp[49],
                                          "FS_ITEM2"        => $tmp[50],
                                          "FS_ITEM3"        => $tmp[51],
                                          "FS_ITEM4"        => $tmp[52],
                                          "FS_ITEM5"        => $tmp[53],
                                          "FS_ORG_SHDIV1"      => $tmp[54],
                                          "FS_ORG_MAJORCD1"    => $tmp[55],
                                          "FS_ORG_JUDGEMENT1"  => $tmp[56],
                                          "FS_SHDIV1"          => $tmp[57],
                                          "FS_COURSECD1"       => $tmp[58],
                                          "FS_MAJORCD1"        => $tmp[59],
                                          "FS_EXAMCOURSECD1"   => $tmp[60],
                                          "FS_JUDGEMENT1"      => $tmp[61],
                                          "FS_ORG_SHDIV2"      => $tmp[62],
                                          "FS_ORG_MAJORCD2"    => $tmp[63],
                                          "FS_ORG_JUDGEMENT2"  => $tmp[64],
                                          "FS_SHDIV2"          => $tmp[65],
                                          "FS_COURSECD2"       => $tmp[66],
                                          "FS_MAJORCD2"        => $tmp[67],
                                          "FS_EXAMCOURSECD2"   => $tmp[68],
                                          "FS_JUDGEMENT2"      => $tmp[69],
                                          "FS_ORG_SHDIV3"      => $tmp[70],
                                          "FS_ORG_MAJORCD3"    => $tmp[71],
                                          "FS_ORG_JUDGEMENT3"  => $tmp[72],
                                          "FS_SHDIV3"          => $tmp[73],
                                          "FS_COURSECD3"       => $tmp[74],
                                          "FS_MAJORCD3"        => $tmp[75],
                                          "FS_EXAMCOURSECD3"   => $tmp[76],
                                          "FS_JUDGEMENT3"      => $tmp[77],
                                          "FS_ORG_SHDIV4"      => $tmp[78],
                                          "FS_ORG_MAJORCD4"    => $tmp[79],
                                          "FS_ORG_JUDGEMENT4"  => $tmp[80],
                                          "FS_SHDIV4"          => $tmp[81],
                                          "FS_COURSECD4"       => $tmp[82],
                                          "FS_MAJORCD4"        => $tmp[83],
                                          "FS_EXAMCOURSECD4"   => $tmp[84],
                                          "FS_JUDGEMENT4"      => $tmp[85],
                                          "SHDIV1"          => $tmp[86],
                                          "COURSECD1"       => $tmp[87],
                                          "MAJORCD1"        => $tmp[88],
                                          "EXAMCOURSECD1"   => $tmp[89],
                                          "JUDGEMENT1"      => $tmp[90],
                                          "SHDIV2"          => $tmp[91],
                                          "COURSECD2"       => $tmp[92],
                                          "MAJORCD2"        => $tmp[93],
                                          "EXAMCOURSECD2"   => $tmp[94],
                                          "JUDGEMENT2"      => $tmp[95],
                                          "SHDIV3"          => $tmp[96],
                                          "COURSECD3"       => $tmp[97],
                                          "MAJORCD3"        => $tmp[98],
                                          "EXAMCOURSECD3"   => $tmp[99],
                                          "JUDGEMENT3"      => $tmp[100],
                                          "SHDIV4"          => $tmp[101],
                                          "COURSECD4"       => $tmp[102],
                                          "MAJORCD4"        => $tmp[103],
                                          "EXAMCOURSECD4"   => $tmp[104],
                                          "JUDGEMENT4"      => $tmp[105],
                                          $this->lastColumn => $tmp[106]); //LASTCOLUMN OR DUMMY
                            $temp_arr[$i] = $data;
                            break;
                        case "3":  //内申データ
                            $data = array(
                                          "TARGET_TABLE"        => $tmp[0],
                                          "ENTEXAMYEAR"         => $tmp[1],
                                          "TESTDIV"             => $tmp[2],
                                          "EXAMNO"              => $tmp[3],
                                          "CONFIDENTIAL_RPT01"  => $tmp[4],
                                          "CONFIDENTIAL_RPT02"  => $tmp[5],
                                          "CONFIDENTIAL_RPT03"  => $tmp[6],
                                          "CONFIDENTIAL_RPT04"  => $tmp[7],
                                          "CONFIDENTIAL_RPT05"  => $tmp[8],
                                          "CONFIDENTIAL_RPT06"  => $tmp[9],
                                          "CONFIDENTIAL_RPT07"  => $tmp[10],
                                          "CONFIDENTIAL_RPT08"  => $tmp[11],
                                          "CONFIDENTIAL_RPT09"  => $tmp[12],
                                          "CONFIDENTIAL_RPT10"  => $tmp[13],
                                          "CONFIDENTIAL_RPT11"  => $tmp[14],
                                          "CONFIDENTIAL_RPT12"  => $tmp[15],
                                          "TOTAL_REPORT"        => $tmp[16],
                                          "AVERAGE5"            => $tmp[17],
                                          "AVERAGE_ALL"         => $tmp[18],
                                          $this->lastColumn     => $tmp[19]); //LASTCOLUMN OR DUMMY

                               $temp_arr[$i] = $data;
                            break;
                    }
                    $i++;
                }
            }
            fclose($fp);
        } else {
            $this->warning = "ファイル名が不正です。";
        }
        return $temp_arr;
    }

    /* ---------------------------- */
    /* データの整合性チェック       */
    /* ---------------------------- */
    public function isDataMatched(&$data_arr)
    {
        $db = Query::dbCheckOut();

        switch ($this->target) {
            case "1":  //基礎・住所・得点データ
                $data_each = array(
                                   "TARGET_TABLE"            => array(1  , "対象データ*"),
                                   "ENTEXAMYEAR"             => array(4  , "入試年度*"),
                                   "TESTDIV"                 => array(1  , "試験区分*"),
                                   "EXAMNO"                  => array(4  , "受験番号*"),
                                   "SHDIV"                   => array(1  , "専併区分"),
                                   "DESIREDIV"               => array(2  , "志望区分（出願パターン）"),
                                   "APPLICANTDIV"            => array(1  , "出願区分"),
                                   "CLUBCD"                  => array(4  , "クラブコード"),
                                   "SPECIAL_REASON_DIV"      => array(1  , "特別理由区分*"),
                                   "NAME"                    => array(60 , "氏名"),
                                   "NAME_KANA"               => array(180, "氏名かな"),
                                   "SEX"                     => array(1  , "性別コード"),
                                   "BIRTHDAY"                => array(10 , "生年月日"),
                                   "ADDRESSCD"               => array(2  , "現住所コード"),
                                   "TELNO"                   => array(14 , "電話番号"),
                                   "LOCATIONCD"              => array(2  , "出身学校所在地コード"),
                                   "NATPUBPRIDIV"            => array(1  , "国公私立区分コード"),
                                   "FS_CD"                   => array(7  , "出身学校コード"),
                                   "FS_GRDYEAR"              => array(4  , "卒業年"),
                                   "PS_CD"                   => array(7  , "塾コード"),
                                   "APPROVAL_FLG"            => array(1  , "通知承諾フラグ"),
                                   "GNAME"                   => array(60 , "保護者氏名"),
                                   "GKANA"                   => array(180, "保護者かな"),
                                   "GTELNO"                  => array(14 , "保護者電話番号"),
                                   "A_TOTAL"                 => array(3  , "配点Ａ試験合計点"),
                                   "A_AVERAGE"               => array(5  , "配点Ａ試験平均点"),
                                   "A_TOTAL_RANK"            => array(4  , "配点Ａ全体席次"),
                                   "A_DIV_RANK"              => array(4  , "配点Ａ志望区分席次"),
                                   "B_TOTAL"                 => array(3  , "配点Ｂ試験合計点"),
                                   "B_AVERAGE"               => array(5  , "配点Ｂ試験平均点"),
                                   "B_TOTAL_RANK"            => array(4  , "配点Ｂ全体席次"),
                                   "B_DIV_RANK"              => array(4  , "配点Ｂ志望区分席次"),
                                   "JUDGEMENT"               => array(1  , "合否判定"),
                                   "JUDGEMENT_GROUP_NO"      => array(2  , "追加繰上合格グループナンバー"),
                                   "INTERVIEW_ATTEND_FLG"    => array(1  , "面接出欠フラグ"),
                                   "SCALASHIPDIV"            => array(2  , "スカラシップ区分"),
                                   "SUC_COURSECD"            => array(1  , "合格課程コード"),
                                   "SUC_MAJORCD"             => array(3  , "合格学科コード"),
                                   "SUC_COURSECODE"          => array(4  , "合格コースコード"),
                                   "PROCEDUREDIV"            => array(1  , "手続区分"),
                                   "ENTDIV"                  => array(1  , "入学区分"),
                                   "FORMNO"                  => array(6  , "フォーム№"),
                                   "SUCCESS_NOTICENO"        => array(4  , "合格通知№"),
                                   "OLD_SUCCESS_NOTICENO"    => array(4  , "旧合格通知№"),
                                   "FAILURE_NOTICENO"        => array(4  , "結果(不合格)通知№"),
                                   "ZIPCD"                   => array(8  , "郵便番号"),
                                   "ADDRESS"                 => array(150, "住所"),
                                   "GZIPCD"                  => array(8  , "保護者郵便番号"),
                                   "GADDRESS"                => array(150, "保護者住所"),
                                   "TESTSUBCLASSCD1"         => array(1  , "試験科目コード1"),
                                   "ATTEND_FLG1"             => array(1  , "出欠フラグ1"),
                                   "A_SCORE1"                => array(3  , "Ａ得点1"),
                                   "A_STD_SCORE1"            => array(5  , "Ａ偏差値1"),
                                   "A_RANK1"                 => array(4  , "Ａ順位1"),
                                   "B_SCORE1"                => array(3  , "Ｂ得点1"),
                                   "B_STD_SCORE1"            => array(5  , "Ｂ偏差値1"),
                                   "B_RANK1"                 => array(4  , "Ｂ順位1"),
                                   "TESTSUBCLASSCD2"         => array(1  , "試験科目コード2"),
                                   "ATTEND_FLG2"             => array(1  , "出欠フラグ2"),
                                   "A_SCORE2"                => array(3  , "Ａ得点2"),
                                   "A_STD_SCORE2"            => array(5  , "Ａ偏差値2"),
                                   "A_RANK2"                 => array(4  , "Ａ順位"),
                                   "B_SCORE2"                => array(3  , "Ｂ得点2"),
                                   "B_STD_SCORE2"            => array(5  , "Ｂ偏差値2"),
                                   "B_RANK2"                 => array(4  , "Ｂ順位2"),
                                   "TESTSUBCLASSCD3"         => array(1  , "試験科目コード3"),
                                   "ATTEND_FLG3"             => array(1  , "出欠フラグ3"),
                                   "A_SCORE3"                => array(3  , "Ａ得点3"),
                                   "A_STD_SCORE3"            => array(5  , "Ａ偏差値3"),
                                   "A_RANK3"                 => array(4  , "Ａ順位3"),
                                   "B_SCORE3"                => array(3  , "Ｂ得点3"),
                                   "B_STD_SCORE3"            => array(5  , "Ｂ偏差値3"),
                                   "B_RANK3"                 => array(4  , "Ｂ順位3"),
                                   "TESTSUBCLASSCD4"         => array(1  , "試験科目コード4"),
                                   "ATTEND_FLG4"             => array(1  , "出欠フラグ4"),
                                   "A_SCORE4"                => array(3  , "Ａ得点4"),
                                   "A_STD_SCORE4"            => array(5  , "Ａ偏差値4"),
                                   "A_RANK4"                 => array(4  , "Ａ順位4"),
                                   "B_SCORE4"                => array(3  , "Ｂ得点4"),
                                   "B_STD_SCORE4"            => array(5  , "Ｂ偏差値4"),
                                   "B_RANK4"                 => array(4  , "Ｂ順位4"),
                                   "TESTSUBCLASSCD5"         => array(1  , "試験科目コード5"),
                                   "ATTEND_FLG5"             => array(1  , "出欠フラグ5"),
                                   "A_SCORE5"                => array(3  , "Ａ得点5"),
                                   "A_STD_SCORE5"            => array(5  , "Ａ偏差値5"),
                                   "A_RANK5"                 => array(4  , "Ａ順位5"),
                                   "B_SCORE5"                => array(3  , "Ｂ得点5"),
                                   "B_STD_SCORE5"            => array(5  , "Ｂ偏差値5"),
                                   "B_RANK5"                 => array(4  , "Ｂ順位5"),
                                   $this->lastColumn         => array(10 , $this->lastColumn));
                break;
            case "2":  //事前相談データ
                $data_each = array(
                                   "TARGET_TABLE"          => array(1   , "対象データ*"),
                                   "ENTEXAMYEAR"           => array(4   , "入試年度*"),
                                   "EXAMNO"                => array(4   , "受験番号*"),
                                   "TESTDIV"               => array(1   , "試験区分*"),
                                   "ACCEPTNO"              => array(4   , "受付番号*"),
                                   "CREATE_DATE"           => array(10  , "作成日付*"),
                                   "PS_ACCEPTNO"           => array(4   , "塾受付番号*"),
                                   "PS_UPDATED"            => array(10  , "塾登録日付"),
                                   "PS_CD"                 => array(7   , "塾コード"),
                                   "PS_ITEM1"              => array(3   , "塾実力テスト得点"),
                                   "PS_ITEM2"              => array(5   , "塾実力テスト平均"),
                                   "PS_ITEM3"              => array(5   , "塾得点－平均"),
                                   "PS_ITEM4"              => array(5   , "塾五ツ木偏差値"),
                                   "PS_ITEM5"              => array(5   , "塾各種模擬偏差値"),
                                   "PS_ORG_SHDIV1"         => array(1   , "塾専併区分1（ORG）"),
                                   "PS_ORG_MAJORCD1"       => array(1   , "塾学科1（ORG）"),
                                   "PS_ORG_JUDGEMENT1"     => array(1   , "塾判定1（ORG）"),
                                   "PS_SHDIV1"             => array(1   , "塾専併区分1"),
                                   "PS_COURSECD1"          => array(1   , "塾課程コード1"),
                                   "PS_MAJORCD1"           => array(3   , "塾学科コード1"),
                                   "PS_EXAMCOURSECD1"      => array(4   , "塾コースコード1"),
                                   "PS_JUDGEMENT1"         => array(1   , "塾判定1"),
                                   "PS_ORG_SHDIV2"         => array(1   , "塾専併区分2（ORG）"),
                                   "PS_ORG_MAJORCD2"       => array(1   , "塾学科2（ORG）"),
                                   "PS_ORG_JUDGEMENT2"     => array(1   , "塾判定2（ORG）"),
                                   "PS_SHDIV2"             => array(1   , "塾専併区分2"),
                                   "PS_COURSECD2"          => array(1   , "塾課程コード2"),
                                   "PS_MAJORCD2"           => array(3   , "塾学科コード2"),
                                   "PS_EXAMCOURSECD2"      => array(4   , "塾コースコード2"),
                                   "PS_JUDGEMENT2"         => array(1   , "塾判定2"),
                                   "PS_ORG_SHDIV3"         => array(1   , "塾専併区分3（ORG）"),
                                   "PS_ORG_MAJORCD3"       => array(1   , "塾学科3（ORG）"),
                                   "PS_ORG_JUDGEMENT3"     => array(1   , "塾判定3（ORG）"),
                                   "PS_SHDIV3"             => array(1   , "塾専併区分3"),
                                   "PS_COURSECD3"          => array(1   , "塾課程コード3"),
                                   "PS_MAJORCD3"           => array(3   , "塾学科コード3"),
                                   "PS_EXAMCOURSECD3"      => array(4   , "塾コースコード3"),
                                   "PS_JUDGEMENT3"         => array(1   , "塾判定3"),
                                   "PS_ORG_SHDIV4"         => array(1   , "塾専併区分4（ORG）"),
                                   "PS_ORG_MAJORCD4"       => array(1   , "塾学科4（ORG）"),
                                   "PS_ORG_JUDGEMENT4"     => array(1   , "塾判定4（ORG）"),
                                   "PS_SHDIV4"             => array(1   , "塾専併区分4"),
                                   "PS_COURSECD4"          => array(1   , "塾課程コード4"),
                                   "PS_MAJORCD4"           => array(3   , "塾学科コード4"),
                                   "PS_EXAMCOURSECD4"      => array(4   , "塾コースコード4"),
                                   "PS_JUDGEMENT4"         => array(1   , "塾判定4"),
                                   "FS_ACCEPTNO"           => array(4   , "学校受付番号*"),
                                   "FS_UPDATED"            => array(10  , "学校登録日付"),
                                   "FS_CD"                 => array(7   , "学校コード"),
                                   "FS_ITEM1"              => array(3   , "学校実力テスト得点"),
                                   "FS_ITEM2"              => array(5   , "学校実力テスト平均"),
                                   "FS_ITEM3"              => array(5   , "学校得点－平均"),
                                   "FS_ITEM4"              => array(5   , "学校五ツ木偏差値"),
                                   "FS_ITEM5"              => array(5   , "学校各種模擬偏差値"),
                                   "FS_ORG_SHDIV1"         => array(1   , "学校専併区分1（ORG）"),
                                   "FS_ORG_MAJORCD1"       => array(1   , "学校学科1（ORG）"),
                                   "FS_ORG_JUDGEMENT1"     => array(1   , "学校判定1（ORG）"),
                                   "FS_SHDIV1"             => array(1   , "専併区分1"),
                                   "FS_COURSECD1"          => array(1   , "課程コード1"),
                                   "FS_MAJORCD1"           => array(3   , "学科コード1"),
                                   "FS_EXAMCOURSECD1"      => array(4   , "コースコード1"),
                                   "FS_JUDGEMENT1"         => array(1   , "判定1"),
                                   "FS_ORG_SHDIV2"         => array(1   , "学校専併区分2（ORG）"),
                                   "FS_ORG_MAJORCD2"       => array(1   , "学校学科2（ORG）"),
                                   "FS_ORG_JUDGEMENT2"     => array(1   , "学校判定2（ORG）"),
                                   "FS_SHDIV2"             => array(1   , "専併区分2"),
                                   "FS_COURSECD2"          => array(1   , "課程コード2"),
                                   "FS_MAJORCD2"           => array(3   , "学科コード2"),
                                   "FS_EXAMCOURSECD2"      => array(4   , "コースコード2"),
                                   "FS_JUDGEMENT2"         => array(1   , "判定2"),
                                   "FS_ORG_SHDIV3"         => array(1   , "学校専併区分3（ORG）"),
                                   "FS_ORG_MAJORCD3"       => array(1   , "学校学科3（ORG）"),
                                   "FS_ORG_JUDGEMENT3"     => array(1   , "学校判定3（ORG）"),
                                   "FS_SHDIV3"             => array(1   , "専併区分3"),
                                   "FS_COURSECD3"          => array(1   , "課程コード3"),
                                   "FS_MAJORCD3"           => array(3   , "学科コード3"),
                                   "FS_EXAMCOURSECD3"      => array(4   , "コースコード3"),
                                   "FS_JUDGEMENT3"         => array(1   , "判定3"),
                                   "FS_ORG_SHDIV4"         => array(1   , "学校専併区分4（ORG）"),
                                   "FS_ORG_MAJORCD4"       => array(1   , "学校学科4（ORG）"),
                                   "FS_ORG_JUDGEMENT4"     => array(1   , "学校判定4（ORG）"),
                                   "FS_SHDIV4"             => array(1   , "専併区分4"),
                                   "FS_COURSECD4"          => array(1   , "課程コード4"),
                                   "FS_MAJORCD4"           => array(3   , "学科コード4"),
                                   "FS_EXAMCOURSECD4"      => array(4   , "コースコード4"),
                                   "FS_JUDGEMENT4"         => array(1   , "判定4"),
                                   "SHDIV1"                => array(1   , "専併区分1"),
                                   "COURSECD1"             => array(1   , "課程コード1"),
                                   "MAJORCD1"              => array(3   , "学科コード1"),
                                   "EXAMCOURSECD1"         => array(4   , "コースコード1"),
                                   "JUDGEMENT1"            => array(1   , "判定1"),
                                   "SHDIV2"                => array(1   , "専併区分2"),
                                   "COURSECD2"             => array(1   , "課程コード2"),
                                   "MAJORCD2"              => array(3   , "学科コード2"),
                                   "EXAMCOURSECD2"         => array(4   , "コースコード2"),
                                   "JUDGEMENT2"            => array(1   , "判定2"),
                                   "SHDIV3"                => array(1   , "専併区分3"),
                                   "COURSECD3"             => array(1   , "課程コード3"),
                                   "MAJORCD3"              => array(3   , "学科コード3"),
                                   "EXAMCOURSECD3"         => array(4   , "コースコード3"),
                                   "JUDGEMENT3"            => array(1   , "判定3"),
                                   "SHDIV4"                => array(1   , "専併区分4"),
                                   "COURSECD4"             => array(1   , "課程コード4"),
                                   "MAJORCD4"              => array(3   , "学科コード4"),
                                   "EXAMCOURSECD4"         => array(4   , "コースコード4"),
                                   "JUDGEMENT4"            => array(1   , "判定4"),
                                   $this->lastColumn       => array(10  , $this->lastColumn));
                break;
            case "3":  //内申データ
                $data_each = array(
                                   "TARGET_TABLE"            => array(1   , "対象データ*"),
                                   "ENTEXAMYEAR"             => array(4   , "入試年度*"),
                                   "TESTDIV"                 => array(1   , "試験区分*"),
                                   "EXAMNO"                  => array(4   , "受験番号*"),
                                   "CONFIDENTIAL_RPT01"      => array(2   , "教科１"),
                                   "CONFIDENTIAL_RPT02"      => array(2   , "教科２"),
                                   "CONFIDENTIAL_RPT03"      => array(2   , "教科３"),
                                   "CONFIDENTIAL_RPT04"      => array(2   , "教科４"),
                                   "CONFIDENTIAL_RPT05"      => array(2   , "教科５"),
                                   "CONFIDENTIAL_RPT06"      => array(2   , "教科６"),
                                   "CONFIDENTIAL_RPT07"      => array(2   , "教科７"),
                                   "CONFIDENTIAL_RPT08"      => array(2   , "教科８"),
                                   "CONFIDENTIAL_RPT09"      => array(2   , "教科９"),
                                   "CONFIDENTIAL_RPT10"      => array(2   , "教科１０"),
                                   "CONFIDENTIAL_RPT11"      => array(2   , "教科１１"),
                                   "CONFIDENTIAL_RPT12"      => array(2   , "教科１２"),
                                   "TOTAL_REPORT"            => array(4   , "総評"),
                                   "AVERAGE5"                => array(4   , "５教科平均"),
                                   "AVERAGE_ALL"             => array(4   , "全教科平均"),
                                   $this->lastColumn         => array(10  , $this->lastColumn));
                break;
        }

        $count = get_count($data_arr);    //←事前相談もヘッダが１行に変更 2005/08/10
        for ($i = 0; $i < $count; $i++) {
            foreach ($data_each as $key => $val) {
                switch ($this->target) {
                    case "1":  //基礎・住所・得点データ
                        if (in_array($key, array("TARGET_TABLE", "ENTEXAMYEAR", "TESTDIV", "EXAMNO", "SPECIAL_REASON_DIV", $this->lastColumn))) {
                            if (!strlen(trim($data_arr[$i][$key]))) {
                                $this->setWarning("MSG301", "受験番号：".$data_arr[$i]["EXAMNO"]."\\n項目：".$val[1]);
                                return false;
                            }
                        }
                        break 1;
                    case "2":  //事前相談データ
                        if (in_array($key, array("TARGET_TABLE", "ENTEXAMYEAR", "EXAMNO", "TESTDIV", "ACCEPTNO", "CREATE_DATE", "PS_ACCEPTNO", "FS_ACCEPTNO", $this->lastColumn))) {
                            if (!strlen(trim($data_arr[$i][$key]))) {
                                $this->setWarning("MSG301", "受験番号：".$data_arr[$i]["EXAMNO"]."\\n：項目：".$val[1]);
                                return false;
                            }
                        }
                        break 1;
                    case "3":  //内申データ
                        if (in_array($key, array("TARGET_TABLE", "ENTEXAMYEAR", "TESTDIV", "EXAMNO", $this->lastColumn))) {
                            if (!strlen(trim($data_arr[$i][$key]))) {
                                $this->setWarning("MSG301", "受験番号：".$data_arr[$i]["EXAMNO"]."\\n：項目：".$val[1]);
                                return false;
                            }
                        }
                        break 1;
                }

                //文字数チェック
                if (strlen($data_arr[$i][$key]) > $val[0]) {
                    $this->setWarning(
                        MSG915,
                        "( ".$val[1]." )".$val[0]."バイトまで 受験番号：".$data_arr[$i]["EXAMNO"]." 値：".$data_arr[$i][$key]
                    );
                    return false;
                }
            }

            //対象データ
            if ($data_arr[$i]["TARGET_TABLE"] != $this->target) {
                $this->setWarning("MSG913", "( 対象データ )  \\n受験番号：".$data_arr[$i]["EXAMNO"]);
                return false;
            }

            //年度が現在年度+1ではない場合
            if ($data_arr[$i]["ENTEXAMYEAR"] != ""  && $this->year != $data_arr[$i]["ENTEXAMYEAR"]) {
                $this->setWarning("入試年度が不正です \\n受験番号：".$data_arr[$i]["EXAMNO"]);
                return false;
            }

            //受験番号数値チェック
            if ($data_arr[$i]["EXAMNO"] != "" && !is_numeric($data_arr[$i]["EXAMNO"])) {
                $this->setWarning("受験番号は数字のみです \\n受験番号：".$data_arr[$i]["EXAMNO"]);
                return false;
            }
            
            //受験番号桁数チェック
            if (strlen($data_arr[$i]["EXAMNO"]) !== 4) {
                $this->setWarning("受験番号は4桁です \\n受験番号：".$data_arr[$i]["EXAMNO"]);
                return false;
            }
            
            //LASTCOLUMN Or DUMMYの文字チェック
            if (strlen($data_arr[$i][$this->lastColumn]) != 0 && $data_arr[$i][$this->lastColumn] !== $this->lastColumn) {
                $this->setWarning($this->lastColumn."の文字が不正です \\n受験番号：".$data_arr[$i]["EXAMNO"]);
                return false;
            }

            if (is_numeric($data_arr[$i]["BIRTHDAY"])) {
                $date = $data_arr[$i]["BIRTHDAY"];
                if (strlen($date) == 8) {
                    $data_arr[$i]["BIRTHDAY"] = substr($date, 0, 4)."-".substr($date, 4, 2)."-".substr($date, 6, 2);
                } else {
                    $this->setWarning("日付の書式が不正です( 生年月日 ) \\n受験番号：".$data_arr[$i]["EXAMNO"]);
                    return false;
                }
            } elseif ($data_arr[$i]["BIRTHDAY"]!="" && !preg_match("/([0-9]{4})[-\/]([0-9]{1,2})[-\/]([0-9]{1,2})/", $data_arr[$i]["BIRTHDAY"])) {
                $this->setWarning("日付の書式が不正です( 生年月日 ) \\n受験番号：".$data_arr[$i]["EXAMNO"]);
                return false;
            }
            if ($this->isDate($data_arr[$i]["BIRTHDAY"]) == false) {
                $this->setWarning("日付が不正です( 生年月日 ) \\n受験番号：".$data_arr[$i]["EXAMNO"]);
                return false;
            }

            if (is_numeric($data_arr[$i]["CREATE_DATE"])) {
                $date = $data_arr[$i]["CREATE_DATE"];
                if (strlen($date) == 8) {
                    $data_arr[$i]["CREATE_DATE"] = substr($date, 0, 4)."-".substr($date, 4, 2)."-".substr($date, 6, 2);
                } else {
                    $this->setWarning("日付の書式が不正です( 作成日付 ) \\n受験番号：".$data_arr[$i]["EXAMNO"]);
                    return false;
                }
            } elseif ($data_arr[$i]["CREATE_DATE"]!="" && !preg_match("/([0-9]{4})[-\/]([0-9]{1,2})[-\/]([0-9]{1,2})/", $data_arr[$i]["CREATE_DATE"])) {
                $this->setWarning("日付の書式が不正です( 作成日付 ) \\n受験番号：".$data_arr[$i]["EXAMNO"]);
                return false;
            }
            if ($this->isDate($data_arr[$i]["CREATE_DATE"]) == false) {
                $this->setWarning("日付の書式が不正です( 作成日付 ) \\n受験番号：".$data_arr[$i]["EXAMNO"]);
                return false;
            }

            if (is_numeric($data_arr[$i]["FS_UPDATED"])) {
                $date = $data_arr[$i]["FS_UPDATED"];
                if (strlen($date) == 8) {
                    $data_arr[$i]["FS_UPDATED"] = substr($date, 0, 4)."-".substr($date, 4, 2)."-".substr($date, 6, 2);
                } else {
                    $this->setWarning("日付の書式が不正です( 登録日付 ) \\n受験番号：".$data_arr[$i]["EXAMNO"]);
                    return false;
                }
            } elseif ($data_arr[$i]["FS_UPDATED"]!="" && !preg_match("/([0-9]{4})[-\/]([0-9]{1,2})[-\/]([0-9]{1,2})/", $data_arr[$i]["FS_UPDATED"])) {
                $this->setWarning("日付の書式が不正です( 登録日付 ) \\n受験番号：".$data_arr[$i]["EXAMNO"]);
                return false;
            }
            if ($this->isDate($data_arr[$i]["FS_UPDATED"]) == false) {
                $this->setWarning("日付が不正です( 登録日付 ) \\n受験番号：".$data_arr[$i]["EXAMNO"]);
                return false;
            }

            if (is_numeric($data_arr[$i]["PS_UPDATED"])) {
                $date = $data_arr[$i]["PS_UPDATED"];
                if (strlen($date) == 8) {
                    $data_arr[$i]["PS_UPDATED"] = substr($date, 0, 4)."-".substr($date, 4, 2)."-".substr($date, 6, 2);
                } else {
                    $this->setWarning("日付の書式が不正です( 登録日付 ) \\n受験番号：".$data_arr[$i]["EXAMNO"]);
                    return false;
                }
            } elseif ($data_arr[$i]["PS_UPDATED"]!="" && !preg_match("/([0-9]{4})[-\/]([0-9]{1,2})[-\/]([0-9]{1,2})/", $data_arr[$i]["PS_UPDATED"])) {
                $this->setWarning("日付の書式が不正です( 登録日付 ) \\n受験番号：".$data_arr[$i]["EXAMNO"]);
                return false;
            }
            if ($this->isDate($data_arr[$i]["PS_UPDATED"]) == false) {
                $this->setWarning("日付が不正です( 登録日付 ) \\n受験番号：".$data_arr[$i]["EXAMNO"]);
                return false;
            }

            //データ型チェック
            for ($scoreCnt = 1; $scoreCnt <= 5; $scoreCnt++) {
                if (($data_arr[$i]["A_SCORE".$scoreCnt] != "" && !is_numeric($data_arr[$i]["A_SCORE".$scoreCnt])) ||
                    ($data_arr[$i]["B_SCORE".$scoreCnt] != "" && !is_numeric($data_arr[$i]["B_SCORE".$scoreCnt]))) {
                    $this->warning = "Ａ得点かＢ得点のいずれかが数値ではありません。\\n受験番号：".$data_arr[$i]["EXAMNO"];
                    return false;
                }
            }

            //2：事前相談データのみ
            if ($this->target == "2") {
                if ($data_arr[$i]["PS_ITEM1"] != "" && !is_numeric($data_arr[$i]["PS_ITEM1"])) {
                    $this->warning = "塾の実力テスト得点が数値ではありません。\\n氏名：".$data_arr[$i]["NAME"];
                    return false;
                } elseif ($data_arr[$i]["FS_ITEM1"] != "" && !is_numeric($data_arr[$i]["FS_ITEM1"])) {
                    $this->warning = "出身学校の実力テスト得点が数値ではありません。\\n氏名：".$data_arr[$i]["NAME"];
                    return false;
                }
                for ($pitem = 2; $pitem < 6; $pitem++) {
                    $item = explode(".", $data_arr[$i]["PS_ITEM".$pitem]);
                    if ($data_arr[$i]["PS_ITEM".$pitem] != "" & !(preg_match("/./", $data_arr[$i]["PS_ITEM".$pitem]) & is_numeric($data_arr[$i]["PS_ITEM".$pitem]))) {
                        $this->warning = "塾の平均か偏差値が数値ではありません。\\n氏名：".$data_arr[$i]["NAME"];
                        return false;
                    }
                    if ($item[0] > 999 || $item[0] < -999) {
                        $this->setWarning("MSG915", "( 塾の平均か偏差値は1,000以下です。)  \\n氏名：".$data_arr[$i]["NAME"]."　値：".$item[0]);
                        return false;
                    }
                }
                for ($fitem = 2; $fitem < 6; $fitem++) {
                    $item = explode(".", $data_arr[$i]["FS_ITEM".$fitem]);
                    if ($data_arr[$i]["FS_ITEM".$fitem] != "" & !(preg_match("/./", $data_arr[$i]["FS_ITEM".$fitem]) & is_numeric($data_arr[$i]["FS_ITEM".$fitem]))) {
                        $this->warning = "出身学校の平均か偏差値が数値ではありません。\\n氏名：".$data_arr[$i]["NAME"];
                        return false;
                    }
                    if ($item[0] > 999 || $item[0] < -999) {
                        $this->setWarning("MSG915", "( 出身学校の平均か偏差値は1,000以下です。)  \\n氏名：".$data_arr[$i]["NAME"]."　値：".$item[0]);
                        return false;
                    }
                }
            }

            //3:内申データ
            if ($this->target == "3") {
                for ($rpt = 1; $rpt < 13; $rpt++) {
                    if ($data_arr[$i]["CONFIDENTIAL_RPT".$rpt] != "" && !is_numeric($data_arr[$i]["CONFIDENTIAL_RPT".$rpt])) {
                        $this->warning = "教科".$rpt."が数値ではありません。\\n受験番号：".$data_arr[$i]["EXAMNO"];
                        return false;
                    }
                }
                $total_report = explode(".", $data_arr[$i]["TOTAL_REPORT"]);
                if ($data_arr[$i]["TOTAL_REPORT"] != "" & !(preg_match("/./", $data_arr[$i]["TOTAL_REPORT"]) & is_numeric($data_arr[$i]["TOTAL_REPORT"]))) {
                    $this->warning = "総評が数値ではありません。\\n受験番号：".$data_arr[$i]["EXAMNO"];
                    return false;
                }
                if (strlen($total_report[0]) > 2) {
                    $this->setWarning("MSG915", "(総評)  受験番号：".$data_arr[$i]["EXAMNO"]);
                    return false;
                }
                $average5 = explode(".", $data_arr[$i]["AVERAGE5"]);
                if ($data_arr[$i]["AVERAGE5"] != "" & !(preg_match("/./", $data_arr[$i]["AVERAGE5"]) & is_numeric($data_arr[$i]["AVERAGE5"]))) {
                    $this->warning = "５教科平均が数値ではありません。\\n受験番号：".$data_arr[$i]["EXAMNO"];
                    return false;
                }
                if (strlen($average5[0]) > 2) {
                    $this->setWarning("MSG915", "(５教科平均)  受験番号：".$data_arr[$i]["EXAMNO"]);
                    return false;
                }
                $avarage_all = explode(".", $data_arr[$i]["AVARAGE_ALL"]);
                if ($data_arr[$i]["AVARAGE_ALL"] != "" & !(preg_match("/./", $data_arr[$i]["AVARAGE_ALL"]) & is_numeric($data_arr[$i]["AVARAGE_ALL"]))) {
                    $this->warning = "全教科平均が数値ではありません。\\n受験番号：".$data_arr[$i]["EXAMNO"];
                    return false;
                }
                if (strlen($avarage_all[0]) > 2) {
                    $this->setWarning("MSG915", "( 全教科平均 )  受験番号：".$data_arr[$i]["EXAMNO"]);
                    return false;
                }
            }
        }

        Query::dbCheckIn($db);
        return true;
    }

    //日付の妥当性チェック
    public function isDate($datestr)
    {
        if ($datestr =="") {
            return true;
        }
        $tmp = explode(substr($datestr, 4, 1), $datestr);
        return checkdate($tmp[1], $tmp[2], $tmp[0]);
    }

    //テンプレートの書き出し
    public function outputTmpFile()
    {
        switch ($this->target) {
            case "1":   //基礎・住所・得点データ
                $contents  ="※対象データ,※入試年度,※試験区分,※受験番号,専併区分,";
                $contents .="志望区分（出願パターン）,出願区分,クラブコード,※特別理由区分,";
                $contents .="氏名,氏名かな,性別コード,生年月日,現住所コード,電話番号,";
                $contents .="出身学校所在地コード,国公私立区分コード,出身学校コード,卒業年,塾コード,";
                $contents .="通知承諾フラグ,保護者氏名,保護者かな,保護者電話番号,";
                $contents .="配点Ａ試験合計点,配点Ａ試験平均点,配点Ａ全体席次,配点Ａ志望区分席次,";
                $contents .="配点Ｂ試験合計点,配点Ｂ試験平均点,配点Ｂ全体席次,配点Ｂ志望区分席次,";
                $contents .="合否判定,追加繰上合格グループナンバー,面接出欠フラグ,スカラシップ区分,";
                $contents .="合格課程コード,合格学科コード,合格コースコード,手続区分,入学区分,";
                $contents .="フォーム№,合格通知№,旧合格通知№,結果(不合格)通知№,郵便番号,住所,";
                $contents .="保護者郵便番号,保護者住所,試験科目コード,出欠フラグ,";
                $contents .="Ａ得点,Ａ偏差値,Ａ順位,Ｂ得点,Ｂ偏差値,Ｂ順位,試験科目コード,出欠フラグ,";
                $contents .="Ａ得点,Ａ偏差値,Ａ順位,Ｂ得点,Ｂ偏差値,Ｂ順位,試験科目コード,出欠フラグ,";
                $contents .="Ａ得点,Ａ偏差値,Ａ順位,Ｂ得点,Ｂ偏差値,Ｂ順位,試験科目コード,出欠フラグ,";
                $contents .="Ａ得点,Ａ偏差値,Ａ順位,Ｂ得点,Ｂ偏差値,Ｂ順位,試験科目コード,出欠フラグ,";
                $contents .="Ａ得点,Ａ偏差値,Ａ順位,Ｂ得点,Ｂ偏差値,Ｂ順位,".$this->lastColumn."\n";
                $contents .="固定’1’,半角4桁,半角1桁,半角4桁,半角1桁,半角2桁,半角1桁,半角4桁,";
                $contents .="半角1桁,全角20文字,全角60文字,半角1桁,半角10桁,半角2桁,半角14桁,";
                $contents .="半角2桁,半角1桁,半角7桁,半角4桁,半角7桁,半角1桁,全角20文字,全角60文字,";
                $contents .="半角14桁,半角3桁,整数部3桁、小数部1桁,半角4桁,半角4桁,半角3桁,";
                $contents .="整数部3桁、小数部1桁,半角4桁,半角4桁,半角1桁,半角2桁,半角1桁,半角2桁,";
                $contents .="半角1桁,半角3桁,半角4桁,半角1桁,半角1桁,半角6桁,半角4桁,半角4桁,";
                $contents .="半角4桁,半角8桁,全角50文字,半角8桁,全角50文字,半角1桁,半角1桁,";
                $contents .="半角3桁,整数部3桁、小数部1桁,半角4桁,半角3桁,整数部3桁、小数部1桁,";
                $contents .="半角4桁,半角1桁,半角1桁,半角3桁,整数部3桁、小数部1桁,半角4桁,半角3桁,";
                $contents .="整数部3桁、小数部1桁,半角4桁,半角1桁,半角1桁,半角3桁,整数部3桁、小数部1桁,";
                $contents .="半角4桁,半角3桁,整数部3桁、小数部1桁,半角4桁,半角1桁,半角1桁,半角3桁,";
                $contents .="整数部3桁、小数部1桁,半角4桁,半角3桁,整数部3桁、小数部1桁,半角4桁,";
                $contents .="半角1桁,半角1桁,半角3桁,整数部3桁、小数部1桁,半角4桁,半角3桁,";
                $contents .="整数部3桁、小数部1桁,半角4桁,".$this->lastColumn."\n";
                $filename = sprintf("%s.csv", "基礎・住所・得点データ");
                break;
            case "2":   //事前相談データ
                $contents  ="※対象データ,※入試年度,※受験番号,※試験区分,※受付番号,※作成日付,※塾受付番号,";
                $contents .="塾登録日付,塾コード,塾実力テスト得点,塾実力テスト平均,塾得点－平均,";
                $contents .="塾五ツ木偏差値,塾各種模擬偏差値,塾専併区分1（ORG）,塾学科1（ORG）,";
                $contents .="塾判定1（ORG）,塾専併区分1,塾課程コード1,塾学科コード1,塾コースコード1,塾判定1,";
                $contents .="塾専併区分2（ORG）,塾学科2（ORG）,塾判定2（ORG）,塾専併区分2,塾課程コード2,";
                $contents .="塾学科コード2,塾コースコード2,塾判定2,塾専併区分3（ORG）,塾学科3（ORG）,";
                $contents .="塾判定3（ORG）,塾専併区分3,塾課程コード3,塾学科コード3,塾コースコード3,塾判定3,";
                $contents .="塾専併区分4（ORG）,塾学科4（ORG）,塾判定4（ORG）,塾専併区分4,塾課程コード4,";
                $contents .="塾学科コード4,塾コースコード4,塾判定4,※学校受付番号,学校登録日付,学校コード,";
                $contents .="学校実力テスト得点,学校実力テスト平均,学校得点－平均,学校五ツ木偏差値,";
                $contents .="学校各種模擬偏差値,学校専併区分1（ORG）,学校学科1（ORG）,学校判定1（ORG）,";
                $contents .="専併区分1,課程コード1,学科コード1,コースコード1,判定1,";
                $contents .="学校専併区分2（ORG）,学校学科2（ORG）,学校判定2（ORG）,専併区分2,";
                $contents .="課程コード2,学科コード2,コースコード2,判定2,学校専併区分3（ORG）,";
                $contents .="学校学科3（ORG）,学校判定3（ORG）,専併区分3,課程コード3,学科コード3,";
                $contents .="コースコード3,判定3,学校専併区分4（ORG）,学校学科4（ORG）,学校判定4（ORG）,";
                $contents .="専併区分4,課程コード4,学科コード4,コースコード4,判定4,専併区分1,";
                $contents .="課程コード1,学科コード1,コースコード1,判定1,専併区分2,課程コード2,";
                $contents .="学科コード2,コースコード2,判定2,専併区分3,課程コード3,学科コード3,";
                $contents .="コースコード3,判定3,専併区分4,課程コード4,学科コード4,コースコード4,判定4,".$this->lastColumn."\n";
                $contents .="固定’2’,半角4桁,半角4桁,半角1桁,半角4桁,半角10桁,半角4桁,半角10桁,半角7桁,整数部3桁,";
                $contents .="整数部3桁、小数部1桁,整数部3桁、小数部1桁,整数部3桁、小数部1桁,";
                $contents .="整数部3桁、小数部1桁,全角英字１小文字,〃,〃,半角1桁,半角1桁,半角3桁,半角4桁,";
                $contents .="半角1桁,全角英字１小文字,〃,〃,半角1桁,半角1桁,半角3桁,半角4桁,半角1桁,";
                $contents .="全角英字１小文字,〃,〃,半角1桁,半角1桁,半角3桁,半角4桁,半角1桁,";
                $contents .="全角英字１小文字,〃,〃,半角1桁,半角1桁,半角3桁,半角4桁,半角1桁,半角4桁,";
                $contents .="半角10桁,半角7桁,整数部3桁,整数部3桁、小数部1桁,整数部3桁、小数部1桁,";
                $contents .="整数部3桁、小数部1桁,整数部3桁、小数部1桁,全角英字１小文字,〃,〃,半角1桁,";
                $contents .="半角1桁,半角3桁,半角4桁,半角1桁,全角英字１小文字,〃,〃,半角1桁,半角1桁,半角3桁,";
                $contents .="半角4桁,半角1桁,全角英字１小文字,〃,〃,半角1桁,半角1桁,半角3桁,半角4桁,半角1桁,";
                $contents .="全角英字１小文字,〃,〃,半角1桁,半角1桁,半角3桁,半角4桁,半角1桁,半角1桁,半角1桁,";
                $contents .="半角3桁,半角4桁,半角1桁,半角1桁,半角1桁,半角3桁,半角4桁,半角1桁,半角1桁,半角1桁,";
                $contents .="半角3桁,半角4桁,半角1桁,半角1桁,半角1桁,半角3桁,半角4桁,半角1桁,".$this->lastColumn."\n";
                $filename = sprintf("%s.csv", "事前相談データ");
                break;
            case "3":   //内申データ
                $contents  ="※対象データ,※入試年度,※試験区分,※受験番号,教科１,教科２,教科３,教科４,教科５,教科６,教科７,";
                $contents .="教科８,教科９,教科１０,教科１１,教科１２,総評,５教科平均,全教科平均,".$this->lastColumn."\n";
                $contents .="固定’3’,半角4桁,半角1桁,半角4桁,半角2桁,半角2桁,半角2桁,半角2桁,半角2桁,半角2桁,半角2桁,";
                $contents .="半角2桁,半角2桁,半角2桁,半角2桁,半角2桁,整数部2桁、小数部1桁,";
                $contents .="整数部2桁、小数部1桁,整数部2桁、小数部1桁,".$this->lastColumn."\n";

                $filename = sprintf("%s.csv", "内申データ");
                break;
        }
        common::downloadContents($contents, $filename);
        return true;
    }
}
