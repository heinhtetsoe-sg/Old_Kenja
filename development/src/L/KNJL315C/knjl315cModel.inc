<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl315cModel extends Model
{
    public $field      = array();
    public $cmd;
    public $ObjYear;
    public $control;   //コントロールマスタ
    public $staffcd;   //職員コード
    public $isGojou;
    public $isCollege;
    public $isWakayama;

    /**
     * メンバ変数の初期化
     */
    public function knjl315cModel()
    {
    }

    public function init()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear      = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),          //入試区分
                             "SHDIV"        =>  VARS::post("SHDIV")             //専併区分
                            );

        $this->cmd         = VARS::request("cmd");

        //学校コードを取得：五条・和歌山を判断するため
        $rtnRow = knjl315cQuery::getNameMstZ010();
        $this->isGojou = $rtnRow["NAME2"] == "30290053001" || $rtnRow["NAME2"] == "30290086001" ? true : false;
        $this->isCollege = $rtnRow["NAME2"] == "30290086001" ? true : false;
        $this->isWakayama = $rtnRow["NAME2"] == "30300049001" ? true : false;
    }

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //ファイル名
        $fileName   = "得点分布表";
        //ＣＳＶデータ取得
        if ($this->isCollege && $this->field["APPLICANTDIV"] == "1" && $this->field["TESTDIV"] == "1") {
            $contents  = $this->getDownload($fileName, $this->field["TESTDIV"], $this->field["SHDIV"], "2"); //印刷２科型　国語、算数
            $contents .= $this->getDownload($fileName, $this->field["TESTDIV"], $this->field["SHDIV"], "3"); //印刷３科型　国語、算数、理科
        } elseif ($this->field["TESTDIV"] != "9") {
            $contents  = $this->getDownload($fileName, $this->field["TESTDIV"], $this->field["SHDIV"]);
        } elseif ($this->field["APPLICANTDIV"] == "1") {
            $contents  = $this->getDownload($fileName, "1", "1");
            $contents .= $this->getDownload($fileName, "2", "1");
        } else {
            $contents  = $this->getDownload($fileName, "3", "1");
            $contents .= $this->getDownload($fileName, "3", "2");
            $contents .= $this->getDownload($fileName, "4", "1");
        }
        //ＣＳＶデータ出力
        if (strlen($contents)) {
            $filename = sprintf("%s.csv", $fileName);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    //ＣＳＶ出力
    public function getDownload($fileName, $testdiv, $shdiv, $sub2ka3kaDiv = "")
    {
        //DB接続
        $db = Query::dbCheckOut();

        //年度取得
        $nxtyr_date = (substr(CTRL_DATE, 0, 4)+1).(substr(CTRL_DATE, 4));
        $year = common::DateConv1(str_replace("-", "/", $nxtyr_date), 10);
        //日付・時間取得
        $date = common::DateConv1(str_replace("-", "/", CTRL_DATE), 0);
        //名称マスタ
        $appdivName  = $db->getOne(knjl315cQuery::getNameMst("L003", $this->field["APPLICANTDIV"]));
        $testdivName = $db->getOne(knjl315cQuery::getNameMst("L004", $testdiv));
        $shdivName   = "（" .$db->getOne(knjl315cQuery::getNameMst("L006", $shdiv)) ."）";
        if (!(($this->field["APPLICANTDIV"] == "2" && $testdiv == "3") || $this->isGojou)) {
            $shdivName = "";
        }
        if ($this->isGojou && $this->field["APPLICANTDIV"] == "1" || $this->isCollege && $this->field["APPLICANTDIV"] == "2") {
            $shdivName = "";
        }
        //タイトル
        $sub2ka3kaDivName = "";
        if ($sub2ka3kaDiv == "2") {
            $sub2ka3kaDivName = "（２科型）";
        }
        if ($sub2ka3kaDiv == "3") {
            $sub2ka3kaDivName = "（３科型）";
        }
        $title      = $year ."　" .$appdivName ."　" .$testdivName ."入学試験" ."　" .$shdivName ."　" .$fileName ."　" .$sub2ka3kaDivName;

        //テスト科目
        $arrTestSubclass150 = $arrTestSubclass100 = $ruikeiSub = array();
        $query = knjl315cQuery::getTestSubclass($this->ObjYear, $this->field["APPLICANTDIV"], $testdiv, $sub2ka3kaDiv);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["PERFECT"] == 150 && !$this->isWakayama) {
                $arrTestSubclass150[$row["TESTSUBCLASSCD"]] = $row["TESTSUBCLASSNAME"];
            } elseif ($row["PERFECT"] == 100 || $this->isWakayama) {
                $arrTestSubclass100[$row["TESTSUBCLASSCD"]] = $row["TESTSUBCLASSNAME"];
            }
            $ruikeiSub[$row["TESTSUBCLASSCD"]] = 0;
        }

        //ヘッダ--------------
        $contents  = $title ."\n";
        $contents .= $date ."\n";
        $header1 = array();
        $header1[] = "合計";
        $contents .= ",";
        foreach ($header1 as $code => $name) {
            $contents .= $name ."人数" .",";
            $contents .= $name ."累計" .",";
        }
        //五条高校、和歌山は満点150欄をカット
        if ((!$this->isGojou || $this->field["APPLICANTDIV"] == "1") && !$this->isWakayama) {
            $contents .= ",,";
            foreach ($arrTestSubclass150 as $code => $name) {
                $contents .= $name ."人数" .",";
                $contents .= $name ."累計" .",";
            }
        }
        $contents .= ",,";
        foreach ($arrTestSubclass100 as $code => $name) {
            $contents .= $name ."人数" .",";
            $contents .= $name ."累計" .",";
        }
        $contents .= "\n";

        //SQL文発行（合計）
        $data = false;
        $dataCount = array();
        $query = knjl315cQuery::selectCsvQueryTotal($this->ObjYear, $this->field["APPLICANTDIV"], $testdiv, $shdiv, $this->isGojou, $sub2ka3kaDiv, $this->isCollege, $this->isWakayama);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $dataCount[$row["DISTINDEX"]] = $row["COUNT"];
            $data = true;
        }
        $result->free();

        //SQL文発行（科目）
        $data = false;
        $dataCount150 = $dataCount100 = array();
        $query = knjl315cQuery::selectCsvQuerySubclass($this->ObjYear, $this->field["APPLICANTDIV"], $testdiv, $shdiv, $this->isGojou, $sub2ka3kaDiv, $this->isCollege);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            if ($row["PERFECT"] == 150 && !$this->isWakayama) {
                $dataCount150[$row["DISTINDEX"]][$row["TESTSUBCLASSCD"]] = $row["COUNT"];
            } elseif ($row["PERFECT"] == 100 || $this->isWakayama) {
                $dataCount100[$row["DISTINDEX"]][$row["TESTSUBCLASSCD"]] = $row["COUNT"];
            }
            $data = true;
        }
        $result->free();

        //配列（合計）
        $arrBunpu500 = array();
        $perfect = $this->isGojou ? 400 : $this->isWakayama ? 300 : 500;
        $kizami  = 10;
        $maxIndex = $this->isGojou ? 40 : $this->isWakayama ? 30 : 50;
        for ($index = 0; $index <= $maxIndex; $index++) {
            if ($index == 0) {
                $arrBunpu500[$index] = $perfect;
            } else {
                $fromBunpu  = $perfect - ($kizami * $index);
                $toBunpu    = $fromBunpu + ($kizami - 1);
                $arrBunpu500[$index] = $fromBunpu ." ～ " .$toBunpu;
            }
        }
        $arrBunpu500[777] = "人員";
        $arrBunpu500[888] = "平均";
        //配列（科目150）
        $arrBunpu150 = array();
        $perfect = 150;
        $kizami  = 5;
        $maxIndex = 30;
        for ($index = 0; $index <= $maxIndex; $index++) {
            if ($index == 0) {
                $arrBunpu150[$index] = $perfect;
            } else {
                $fromBunpu  = $perfect - ($kizami * $index);
                $toBunpu    = $fromBunpu + ($kizami - 1);
                $arrBunpu150[$index] = $fromBunpu ." ～ " .$toBunpu;
            }
        }
        $arrBunpu150[777] = "人員";
        $arrBunpu150[888] = "平均";
        //配列（科目100）
        $arrBunpu100 = array();
        $perfect = 100;
        $kizami  = 5;
        $maxIndex = 20;
        for ($index = 0; $index <= $maxIndex; $index++) {
            if ($index == 0) {
                $arrBunpu100[$index] = $perfect;
            } else {
                $fromBunpu  = $perfect - ($kizami * $index);
                $toBunpu    = $fromBunpu + ($kizami - 1);
                $arrBunpu100[$index] = $fromBunpu ." ～ " .$toBunpu;
            }
        }
        $arrBunpu100[777] = "人員";
        $arrBunpu100[888] = "平均";

        //CSV
        $ruikei = 0;
        foreach ($arrBunpu500 as $index => $bunpu) {
            //合計------------------------------------------
            $contents .= $arrBunpu500[$index] .",";
            //人数
            if (strlen($dataCount[$index])) {
                $contents .= $dataCount[$index] .",";
                $ruikei += $dataCount[$index];
            } else {
                $contents .= "0" .",";
                $ruikei += 0;
            }
            //累計
            if ($index == 777 || $index == 888) {
                $ruikei = "";
            }
            $contents .= $ruikei .",";
            //科目150---------------------------------------
            //五条高校、和歌山は満点150欄をカット
            if ((!$this->isGojou || $this->field["APPLICANTDIV"] == "1") && !$this->isWakayama) {
                if ($index <= 30) {
                    $contents .= ",";
                    $contents .= $arrBunpu150[$index] .",";
                    foreach ($arrTestSubclass150 as $code => $name) {
                        //人数
                        if (strlen($dataCount150[$index][$code])) {
                            $contents .= $dataCount150[$index][$code] .",";
                            $ruikeiSub[$code] += $dataCount150[$index][$code];
                        } else {
                            $contents .= "0" .",";
                            $ruikeiSub[$code] += 0;
                        }
                        //累計
                        $contents .= $ruikeiSub[$code] .",";
                    }
                } elseif ($index <= 31) {
                    //人員
                    $contents .= ",";
                    $contents .= $arrBunpu150[777] .",";
                    foreach ($arrTestSubclass150 as $code => $name) {
                        if (strlen($dataCount150[777][$code])) {
                            $contents .= $dataCount150[777][$code] .",,";
                        } else {
                            $contents .= ",,";
                        }
                    }
                } elseif ($index <= 32) {
                    //平均
                    $contents .= ",";
                    $contents .= $arrBunpu150[888] .",";
                    foreach ($arrTestSubclass150 as $code => $name) {
                        if (strlen($dataCount150[888][$code])) {
                            $contents .= $dataCount150[888][$code] .",,";
                        } else {
                            $contents .= ",,";
                        }
                    }
                }
            }
            //科目100---------------------------------------
            if (get_count($arrTestSubclass100) > 0) {
                if ($index <= 20) {
                    $contents .= ",";
                    $contents .= $arrBunpu100[$index] .",";
                    foreach ($arrTestSubclass100 as $code => $name) {
                        //人数
                        if (strlen($dataCount100[$index][$code])) {
                            $contents .= $dataCount100[$index][$code] .",";
                            $ruikeiSub[$code] += $dataCount100[$index][$code];
                        } else {
                            $contents .= "0" .",";
                            $ruikeiSub[$code] += 0;
                        }
                        //累計
                        $contents .= $ruikeiSub[$code] .",";
                    }
                } elseif ($index <= 21) {
                    //人員
                    $contents .= ",";
                    $contents .= $arrBunpu100[777] .",";
                    foreach ($arrTestSubclass100 as $code => $name) {
                        if (strlen($dataCount100[777][$code])) {
                            $contents .= $dataCount100[777][$code] .",,";
                        } else {
                            $contents .= ",,";
                        }
                    }
                } elseif ($index <= 22) {
                    //平均
                    $contents .= ",";
                    $contents .= $arrBunpu100[888] .",";
                    foreach ($arrTestSubclass100 as $code => $name) {
                        if (strlen($dataCount100[888][$code])) {
                            $contents .= $dataCount100[888][$code] .",,";
                        } else {
                            $contents .= ",,";
                        }
                    }
                }
            }
            //改行------------------------------------------
            $contents .= "\n";
        }

        Query::dbCheckIn($db);

        return ($data) ? $contents : "";
    }
}
