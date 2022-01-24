<?php

require_once('for_php7.php');

class knjf074aModel extends Model
{
    public $field      = array();
    public $cmd;
    public $control;       //コントロールマスタ
    public $staffcd;       //職員コード

    public function knjf074aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    public function init()
    {
        $this->field = array("YEAR" =>  VARS::post("YEAR"),  //年度
                             "OUTPUT_CSV"  =>  VARS::post("OUTPUT_CSV"),
                             "OUTPUT_TYPE" =>  VARS::post("OUTPUT_TYPE"),
                             "DBNAME"  =>  VARS::post("DBNAME")    //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");

        $this->shippeiGroupKind = $this->field["OUTPUT_TYPE"];   //1:年齢別  2:性別

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    /**
     * ＣＳＶ出力
     */
    public function getDownloadModel()
    {
        if ($this->field["OUTPUT_CSV"] == "1") {
            //体格統計用CSV
            return $this->outCSVBody();
        } else {
            //疾病統計用CSV
            return $this->outCSVShippei();
        }
    }

    /**
    * 体格統計用CSV
    * 県立高校、特別支援学校で共通のフォーマット
    */
    public function outCSVBody()
    {
        //DB接続
        $db = Query::dbCheckOut();
        $ttlstr = "体格統計用CSV";

        $contents = ",男子,,,女子\n";

        //ヘッダの作成//
        $header = array();

        $header["AGE"]             = "年齢";
        $header["CNT_MAN"]         = "受検者数";
        $header["TOTAL_HEI_MAN"]   = "身長総和";
        $header["TOTAL_WEI_MAN"]   = "体重総和";
        $header["CNT_WOMAN"]       = "受検者数";
        $header["TOTAL_HEI_WOMAN"] = "身長総和";
        $header["TOTAL_WEI_WOMAN"] = "体重総和";

        $contents .= implode($header, ",") ."\n";

        //出力チェックリスト作成
        $kchklist = array();
        $kchklist = array(array("AGE" => "5"),
                          array("AGE" => "6"),
                          array("AGE" => "7"),
                          array("AGE" => "8"),
                          array("AGE" => "9"),
                          array("AGE" => "10"),
                          array("AGE" => "11"),
                          array("AGE" => "12"),
                          array("AGE" => "13"),
                          array("AGE" => "14"),
                          array("AGE" => "15"),
                          array("AGE" => "16"),
                          array("AGE" => "17"));

        //出力データの作成//
        //SQL文発行
        $query = knjf074aQuery::selectBodyCsvQuery();
        $result = $db->query($query);
        $cnt = 0;
        $setDataWk = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);//データ内にカンマがあればドットに置換・・・置換する文字は、カンマでなければ何でも良い。
            $setDataWk[] = $row;
        }
        $cntback = $cnt;
        $rowMaxCnt = 12;
        //チェックリストに登録されているデータと突き合わせて、存在しないデータについては空データを埋める。
        while ($cnt < get_count($kchklist)) {
            if ($rowMaxCnt < $cnt) {
                break;
            }
            $ldat    = $kchklist[$cnt];
            $row     = array();
            $kchkflg = false;
            $kchkstr = $ldat["AGE"];
            foreach ($setDataWk as $rowGet) {
                if ($kchkstr == $rowGet["AGE"]) {
                    $row     = $rowGet;
                    $kchkflg = true;
                    break;
                }
            }

            if (!$kchkflg) {
                foreach ($header as $key => $val) {
                    //ヘッダ部分だけは設定
                    if ($key == "AGE") {
                        if (is_numeric($ldat[$key])) {
                            $setData[$key] = $ldat[$key]."歳";
                        } else {
                            $setData[$key] = $ldat[$key];
                        }
                    } else {
                        //データは空文字
                        $setData[$key] = "";
                    }
                }
            } else {
                foreach ($header as $key => $val) {
                    //キー部分を文字列に変換
                    if ($key == "AGE") {
                        if (is_numeric($row[$key])) {
                            $setData[$key] = $row[$key]."歳";
                        } else {
                            $setData[$key] = $row[$key];
                        }
                    } else {
                        $setData[$key] = $row[$key];
                    }
                }
                $data = true;
            }
            foreach ($header as $key => $val) {
                if (is_numeric($setData[$key])  && $setData[$key] == 0) {
                    $setData[$key] = "";
                }
            }
            $contents .= implode($setData, ",") ."\n";
            $cnt++;
        }

        $result->free();
        Query::dbCheckIn($db);

        if ($data) {
            $couse = $ttlstr;
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは存在しません。";
            return false;
        }
    }

    /**
    * 疾病統計用CSV
    */
    public function outCSVShippei()
    {
        //DB接続
        $db = Query::dbCheckOut();
        $ttlstr = "疾病統計用CSV";

        //学校名称表示
        $schoolname = "";
        $schoolname = $db->getOne(knjf074aQuery::getSchoolName()).",";

        //ヘッダの作成//
        $header = array();

        if ($this->Properties["useSpecial_Support_School"] != "1") {
            //県立高校
            if ($this->shippeiGroupKind == 1) {
                //年齢別
                $contents = $schoolname."高校男子,,,高校女子\n";

                $header["ROWHEADER"] = "";
                $header["M15"] = "15歳";
                $header["M16"] = "16歳";
                $header["M17"] = "17歳";
                $header["F15"] = "15歳";
                $header["F16"] = "16歳";
                $header["F17"] = "17歳";
            } else {
                //性別
                $contents = $schoolname."男子,女子\n";

                $header["ROWHEADER"] = "";
                $header["M"]         = "";
                $header["F"]         = "";
            }
        } else {
            //特別支援学校
            if ($this->shippeiGroupKind == 1) {
                //年齢別
                $contents = $schoolname."幼稚部男子,,,幼稚部女子,,,小学部男子,,,,,,,小学部女子,,,,,,,中学部男子,,,中学部女子,,,高等部男子,,,高等部女子\n";

                $header["ROWHEADER"] = "";
                //幼稚部
                $header["M2"]  = "2歳";
                $header["M3"]  = "3歳";
                $header["M4"]  = "4歳";
                $header["F2"]  = "2歳";
                $header["F3"]  = "3歳";
                $header["F4"]  = "4歳";
                //小学部
                $header["M5"]  = "5歳";
                $header["M6"]  = "6歳";
                $header["M7"]  = "7歳";
                $header["M8"]  = "8歳";
                $header["M9"]  = "9歳";
                $header["M10"] = "10歳";
                $header["M11"] = "11歳";
                $header["F5"]  = "5歳";
                $header["F6"]  = "6歳";
                $header["F7"]  = "7歳";
                $header["F8"]  = "8歳";
                $header["F9"]  = "9歳";
                $header["F10"] = "10歳";
                $header["F11"] = "11歳";
                //中学部
                $header["M12"] = "12歳";
                $header["M13"] = "13歳";
                $header["M14"] = "14歳";
                $header["F12"] = "12歳";
                $header["F13"] = "13歳";
                $header["F14"] = "14歳";
                //高等部
                $header["M15"] = "15歳";
                $header["M16"] = "16歳";
                $header["M17"] = "17歳";
                $header["F15"] = "15歳";
                $header["F16"] = "16歳";
                $header["F17"] = "17歳";
            } else {
                //性別
                $contents = $schoolname."男子,女子\n";

                $header["ROWHEADER"] = "";
                $header["M"]         = "";
                $header["F"]         = "";
            }
        }

        $contents .= implode($header, ",") ."\n";

        //出力データの作成//
        $setDataWk           = array();
        $rowCnt              = 0;
        $PRINT_MAX_ROW_BLOCK = 14;    //CSVに出力するデータのブロック(区間)数

        //ヘッダが1ブロック目なので2ブロック目から
        for ($block = 2; $block <= $PRINT_MAX_ROW_BLOCK; $block++) {
            //SQL文発行
            $query  = knjf074aQuery::selectShippeiCsvQuery($this, $block);
            $result = $db->query($query);

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace(",", ".", $row);//データ内にカンマがあればドットに置換・・・置換する文字は、カンマでなければ何でも良い。

                if ($this->shippeiGroupKind == 1) {
                    //年齢別
                    $setDataWk[] = $row;
                } else {
                    //性別
                    $setDataWk[$rowCnt]["ROWHEADER"] = $row["ROWHEADER"];

                    if ($this->Properties["useSpecial_Support_School"] != "1") {
                        //県立高校
                        $setDataWk[$rowCnt]["M"] = $row["M15"] + $row["M16"] + $row["M17"];
                        $setDataWk[$rowCnt]["F"] = $row["F15"] + $row["F16"] + $row["F17"];
                    } else {
                        //特別支援学校
                        $setDataWk[$rowCnt]["M"] = $row["M2"]  + $row["M3"]  + $row["M4"]  + $row["M5"]
                                                 + $row["M6"]  + $row["M7"]  + $row["M8"]  + $row["M9"]
                                                 + $row["M10"] + $row["M11"] + $row["M12"] + $row["M13"]
                                                 + $row["M14"] + $row["M15"] + $row["M16"] + $row["M17"];
                        $setDataWk[$rowCnt]["F"] = $row["F2"]  + $row["F3"]  + $row["F4"]  + $row["F5"]
                                                 + $row["F6"]  + $row["F7"]  + $row["F8"]  + $row["F9"]
                                                 + $row["F10"] + $row["F11"] + $row["F12"] + $row["F13"]
                                                 + $row["F14"] + $row["F15"] + $row["F16"] + $row["F17"];
                    }
                    $rowCnt++;
                }
            }
        }
        $ROW_MAX_CNT = 54;    //CSVに出力するデータの行数(ヘッダの2行を引いた行数)
        $cnt = 0;
        while ($cnt < $ROW_MAX_CNT) {
            if (($ROW_MAX_CNT) < $cnt) {
                break;
            }

            $row = array();
            $row = $setDataWk[$cnt];
            foreach ($header as $key => $val) {
                $setData[$key] = $row[$key];
                if ($key != "ROWHEADER" && $setData[$key] != 0) {
                    $data = true;
                }
                if (is_numeric($setData[$key])  && $setData[$key] == 0) {
                    $setData[$key] = "";
                }
            }
            $contents .= implode($setData, ",") ."\n";
            $cnt++;
        }

        $result->free();
        Query::dbCheckIn($db);

        if ($data) {
            $couse = $ttlstr;
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは存在しません。";
            return false;
        }
    }
}
