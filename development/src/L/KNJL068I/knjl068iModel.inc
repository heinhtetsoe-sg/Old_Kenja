<?php
class knjl068iModel extends Model
{
    public $field  = array();
    public $cmd;
    public $examYear;

    public function init()
    {
        $this->examYear   = CTRL_YEAR + 1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),          //入試区分
                            );

        $this->cmd = VARS::request("cmd");
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    public function getDownloadModel()
    {

        // fullwidthFlag: true=全角・半角カナ,false=それ以外
        // width: 文字列の長さ
        // kanaFlag: true=半角カナ,false=それ以外
        // padString: 埋める文字(半角の場合のみ指定可)
        $fieldInfo = array(
            'EXAMNO' => array(
                'fullwidthFlag' => false,
                'width' => 10,
                'kanaFlag' => false,
            ),
            // 半角カナ 本人氏名（カナ） 半角カナ20文字
            'NAME_KANA' => array(
                'fullwidthFlag' => true,
                'width' => 20,
                'kanaFlag' => true,
            ),
            // 全角 本人氏名（漢字）
            'NAME' => array(
                'fullwidthFlag' => true,
                'width' => 30,
                'kanaFlag' => false,
            ),
            'SEX' => array(
                'fullwidthFlag' => false,
                'width' => 1,
                'kanaFlag' => false,
            ),
            'BIRTHDAY_1' => array(
                'fullwidthFlag' => false,
                'width' => 6,
                'kanaFlag' => false,
            ),
            'FS_CD_1' => array(
                'fullwidthFlag' => false,
                'width' => 9,
                'kanaFlag' => false,
            ),
            'FS_YM_1' => array(
                'fullwidthFlag' => false,
                'width' => 4,
                'kanaFlag' => false,
            ),
            'FS_CD_2' => array(
                'fullwidthFlag' => false,
                'width' => 9,
                'kanaFlag' => false,
            ),
            'FS_YM_2' => array(
                'fullwidthFlag' => false,
                'width' => 4,
                'kanaFlag' => false,
            ),
            // 半角カナ 保証人氏名（カナ） 半角カナ20文字
            'GKANA' => array(
                'fullwidthFlag' => true,
                'width' => 20,
                'kanaFlag' => true,
            ),
            // 全角 保証人氏名（漢字）
            'GNAME' => array(
                'fullwidthFlag' => true,
                'width' => 30,
                'kanaFlag' => false,
            ),
            'GZIPCD_1' => array(
                'fullwidthFlag' => false,
                'width' => 7,
                'kanaFlag' => false,
            ),
            // 半角 保証人住所（カナ）
            'GADDRESS_KANA' => array(
                'fullwidthFlag' => true,
                'width' => 55,
                'kanaFlag' => true,
            ),
            // 全角 保証人住所（漢字）
            'GADDRESS_1' => array(
                'fullwidthFlag' => true,
                'width' => 100,
                'kanaFlag' => false,
            ),
            'GTELNO' => array(
                'fullwidthFlag' => false,
                'width' => 12,
                'kanaFlag' => false,
            ),
            'FILLER_1' => array(
                'fullwidthFlag' => false,
                'width' => 164,
                'kanaFlag' => false,
            ),
            //34 固定値 1 を出力
            'GOKAKU_FLAG' => array(
                'fullwidthFlag' => false,
                'width' => 1,
                'kanaFlag' => false,
            ),
            //35 固定値 0 を出力
            'REMARK1' => array(
                'fullwidthFlag' => false,
                'width' => 1,
                'kanaFlag' => false,
            ),
            'FILLER_4' => array(
                'fullwidthFlag' => false,
                'width' => 20,
                'kanaFlag' => false,
            ),
            'ZIPCD_1' => array(
                'fullwidthFlag' => false,
                'width' => 7,
                'kanaFlag' => false,
            ),
            // 半角 本人住所カナ
            'ADDRESS_KANA' => array(
                'fullwidthFlag' => true,
                'width' => 55,
                'kanaFlag' => true,
            ),
            // 本人住所（漢字）
            'ADDRESS_1' => array(
                'fullwidthFlag' => true,
                'width' => 100,
                'kanaFlag' => false,
            ),
            'TELNO' => array(
                'fullwidthFlag' => false,
                'width' => 12,
                'kanaFlag' => false,
            ),
            'FILLER_6' => array(
                'fullwidthFlag' => false,
                'width' => 123,
                'kanaFlag' => false,
            ),
        );

        //クリア
        $contents  = "";
        //出力フラグ
        $data = false;

        $db = Query::dbCheckOut();
        $query = knjl068iQuery::selectCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace("\r\n", "", str_replace(",", ".", $row));

            //値をセット
            $sep = "";
            foreach ($fieldInfo as $field => $label) {
                $rowVal = "";
                if (array_key_exists($field, $row)) {
                    $rowVal = $row[$field];
                }
    
                $rowVal = $this->formatCsvVal($rowVal, $fieldInfo[$field]);
                $contents .= $sep.$rowVal;
            }
            $contents .= "\n";

            $data = true;
        }
        $result->free();

        /**************************/
        /*  ファイル名用名称取得  */
        /**************************/
        // 入試制度ファイル名用名称 高等部:KO 中学部:CHU
        $applicantdivName = ($this->field["APPLICANTDIV"] == "2") ? "KO" : "CHU";
        // 入試区分ファイル名用名称 1:A 2:B 3:KIKO 4:S(中学部) or SUI(高等部)
        $testdivName = "";
        switch ($this->field["TESTDIV"]) {
            case 1:
                $testdivName = "A";
                break;
            case 2:
                $testdivName = "B";
                break;
            case 3:
                $testdivName = "KIKO";
                break;
            case 4:
                $testdivName = ($this->field["APPLICANTDIV"] == "2") ? "SUI" : "S";
                break;
            default:
                $testdivName = "";
                break;
        }

        //ファイル名
        $title = $applicantdivName.$testdivName;

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($data) {
            $filename = sprintf("%s.TXT", $title);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    public function formatCsvVal($val, $fieldInfo)
    {
        // 固定値：半角スペース
        $fixedValueHalfWidthSpace = " ";
        // 固定値：全角スペース
        $fixedValueFullWidthSpace = "　";

        // 半角の場合
        if (!$fieldInfo['fullwidthFlag']) {
            $padString = $fixedValueHalfWidthSpace;
            // 埋める文字の指示あり
            if (array_key_exists('padString', $fieldInfo)) {
                $padString = $fieldInfo['padString'];
            }
            $val = str_pad($val, $fieldInfo['width'], $padString);
            // 文字列を指定の長さで切り取り
            $val = substr($val, 0, $fieldInfo['width']);

        // 全角の場合
        } elseif ($fieldInfo['fullwidthFlag']) {
            // カナの場合
            if ($fieldInfo['kanaFlag']) {
                // UTF-8→SJISへ変換
                $val = mb_convert_encoding($val, "SJIS-win", "UTF-8");

                $width = $fieldInfo['width'];
                // 埋める文字（半角スペース）
                $padString = $fixedValueHalfWidthSpace;

                // 文字列を固定長の他の文字列で埋める（マルチバイト対応版）
                $val = $this->mb_str_pad($val, $width*2, $padString, STR_PAD_RIGHT, "SJIS-win");
                // SJIS→UTF-8へ変換
                $val = mb_convert_encoding($val, "UTF-8", "SJIS-win");
                // 全角カナ→半角カナへ変換
                $val = mb_convert_kana($val, "ksha");
                // 文字列を指定の長さで切り取り
                $val = mb_substr($val, 0, $width);

            // 漢字の場合
            } else {
                $isOdd = false;
                $width = $fieldInfo['width'];

                //外字対応 外字を全角の「？」に変換
                $patternGaiji = '/\p{Co}/u';
                $valGaiji = preg_replace($patternGaiji, '？', $val);
                // UTF-8→SJISへ変換
                $valGaiji = mb_convert_encoding($valGaiji, "SJIS-win", "UTF-8");
                //文字列幅 (半角文字:1、 全角文字:2)
                $beforeLength = mb_strwidth($valGaiji, "SJIS-win");

                // UTF-8→SJISへ変換
                $val = mb_convert_encoding($val, "SJIS-win", "UTF-8");
                if ($beforeLength > $width) {
                    // 文字列を指定の長さで切り取り
                    $val = mb_substr($val, 0, $width);
                }

                if ($beforeLength % 2 != 0) {
                    // 奇数の場合
                    $width = $fieldInfo['width'] - 1;
                    $isOdd = true;
                }
                // 埋める文字
                $padString = "#";

                // 文字列を固定長の他の文字列で埋める（マルチバイト対応版）
                $val = $this->mb_str_pad($val, $width, $padString, STR_PAD_RIGHT, "SJIS-win");
                // SJIS→UTF-8へ変換
                $val = mb_convert_encoding($val, "UTF-8", "SJIS-win");
                // 埋める文字*2→全角スペースへ置換
                $pattern = $padString . $padString;

                // 奇数の場合
                if ($isOdd) {
                    $pos = mb_strpos($val, $pattern);
                    if ($pos === false) {
                        $val = mb_ereg_replace($pattern, $fixedValueFullWidthSpace, $val);
                        $val = $val . $fixedValueHalfWidthSpace;
                    } else {
                        $beforeStr = mb_substr($val, 0, $pos);
                        $afterStr = mb_ereg_replace($pattern, $fixedValueFullWidthSpace, mb_substr($val, $pos));
                        // 奇数の場合半角スペースを追加（半角スペース追加位置：文字列最後）
                        $val = $beforeStr.$afterStr.$fixedValueHalfWidthSpace;
                        // 奇数の場合半角スペースを追加（半角スペース追加位置：全角スペースの前）
                        // $val = $beforeStr.$fixedValueHalfWidthSpace.$afterStr;
                    }

                    // 偶数の場合
                } else {
                    $val = mb_ereg_replace($pattern, $fixedValueFullWidthSpace, $val);
                }
            }
        }

        return $val;
    }

    public function mb_str_pad(
        $ps_input,
        $ps_pad_length,
        $ps_pad_string = " ",
        $ps_pad_type = STR_PAD_RIGHT,
        $ps_encoding = 'UTF-8'
    ) {
        return mb_convert_encoding(str_pad(mb_convert_encoding($ps_input, 'SJIS-win', $ps_encoding), $ps_pad_length, $ps_pad_string, $ps_pad_type), $ps_encoding, 'SJIS-win');
    }
}
