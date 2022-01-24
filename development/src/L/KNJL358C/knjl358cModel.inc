<?php

require_once('for_php7.php');


class knjl358cModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $isGojou;
    var $isCollege;

    function knjl358cModel()
    {
    }

    function init()
    {               
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear      = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"), //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),      //入試区分
                             "SHDIV"        =>  VARS::post("SHDIV"),        //専併区分
                             "TARGET"       =>  VARS::post("TARGET"),       //対象者
                             "SORT"         =>  VARS::post("SORT"),         //印刷順序
                             "OUTPUT"       =>  VARS::post("OUTPUT")        //出力フラグ
                            );

        $this->cmd         = VARS::request("cmd");
        //学校コードを取得：五条・和歌山を判断するため
        $rtnRow = knjl358cQuery::getNameMstZ010();
        $this->isGojou = $rtnRow["NAME2"] == "30290053001" || $rtnRow["NAME2"] == "30290086001" ? true : false;
        $this->isCollege = $rtnRow["NAME2"] == "30290086001" ? true : false;
    }

    //ＣＳＶ出力
    function getDownloadModel()
    {
        //対象者 1:受験者 2:合格者 3:入学者
        $targetName = "";
        if ($this->field["TARGET"] == "3") {
            $targetName = "入学者";
        } else if ($this->field["TARGET"] == "2") {
            $targetName = "合格者";
        } else {
            $targetName = "受験者";
        }
        //ファイル名
        $sortName   = ($this->field["SORT"] == "1") ? "（成績順）" : "（受験番号順）";
        $fileName   = $targetName ."成績一覧表" .$sortName;
        //ＣＳＶデータ取得
        if ($this->field["TESTDIV"] != "9") {
            $contents  = $this->getDownload($fileName, $this->field["TESTDIV"], $this->field["SHDIV"]);
        } else if ($this->field["APPLICANTDIV"] == "1") {
            $contents  = $this->getDownload($fileName, "1", "1");
            $contents .= $this->getDownload($fileName, "2", "1");
        } else {
            $contents  = $this->getDownload($fileName, "3", "1");
            $contents .= $this->getDownload($fileName, "3", "2");
            $contents .= $this->getDownload($fileName, "4", "1");
            if ($this->isGojou) {
                $contents .= $this->getDownload($fileName, "7", "1");
                $contents .= $this->getDownload($fileName, "7", "2");
            }
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
    function getDownload($fileName, $testdiv, $shdiv)
    {
        //ヘッダの作成//
        if ($this->isCollege && $this->field["APPLICANTDIV"] == "1" && $testdiv == "1") {
            $header1 = array("NUMBER"            => "No.",
                             "EXAMNO"            => "受験番号",
                             "NAME"              => "生徒氏名",
                             "SEX"               => "性別",
                             "PREF_NAME"         => "県別",
                             "FINSCHOOL_NAME"    => "出身学校",
                             "SUB_ORDER_NAME"    => "受験型"
                            );
            $header2 = array("TOTAL2"            => "２科合計",
                             "AVERAGE2"          => "２科得点率",
                             "TOTAL3"            => "３科合計",
                             "AVERAGE3"          => "３科得点率",
                             "AVERAGE1"          => "得点率",
                             "RANK1"             => "順位",
                             "JUDGENAME"         => "判定",
                             "REMARK"            => "備考"
                            );
        } else {
            $header1 = array("NUMBER"            => "No.",
                             "EXAMNO"            => "受験番号",
                             "NAME"              => "生徒氏名",
                             "SEX"               => "性別",
                             "PREF_NAME"         => "県別",
                             "FINSCHOOL_NAME"    => "出身学校"
                            );
            $header2 = array("TOTAL"             => "合計",
                             "AVERAGE"           => "平均",
                             "RANK"              => "順位",
                             "JUDGENAME"         => "判定",
                             "REMARK"            => "備考"
                            );
        }

        //DB接続
        $db = Query::dbCheckOut();

        //学校名取得
        $certif_kindcd = ($this->field["APPLICANTDIV"] == "1") ? '105' : '106';
        $query3 = knjl358cQuery::getSchoolName($this->ObjYear, $certif_kindcd);
        $schoolname = $db->getOne($query3);
        //年度取得
        $nxtyr_date = (substr(CTRL_DATE,0,4)+1).(substr(CTRL_DATE,4));
        $year = common::DateConv1(str_replace("-","/",$nxtyr_date),10);
        //日付・時間取得
        $date = common::DateConv1(str_replace("-","/",CTRL_DATE),0);
        //名称マスタ
        $appdivName  = $db->getOne(knjl358cQuery::getNameMst("L003", $this->field["APPLICANTDIV"]));
        $testdivName = $db->getOne(knjl358cQuery::getNameMst("L004", $testdiv));
        $shdivName   = "（" .$db->getOne(knjl358cQuery::getNameMst("L006", $shdiv)) ."）";
        if (!(($this->field["APPLICANTDIV"] == "2" && $testdiv == "3") || $this->isGojou)) $shdivName = "";
        if ($this->isGojou && $this->field["APPLICANTDIV"] == "1" || $this->isCollege && $this->field["APPLICANTDIV"] == "2") {
            $shdivName = "";
        }
        //タイトル
        $title      = $year ."　" .$appdivName ."　" .$testdivName .$shdivName ."入学試験" ."　" .$fileName;

        //テスト科目
        $arrTestSubclass = $headerTestSubclass = array();
        $query = knjl358cQuery::getTestSubclass($this->ObjYear, $this->field["APPLICANTDIV"], $testdiv);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arrTestSubclass[$row["TESTSUBCLASSCD"]] = $row["TESTSUBCLASSNAME"];
            $headerTestSubclass[$row["TESTSUBCLASSCD"]] = $row["TESTSUBCLASSNAME"];
        }

        //ヘッダ
        $contents  = $title ."\n";
        $contents .= $date ."　" .$schoolname ."\n";
        $contents .= implode($header1, ",") .",";
        $contents .= implode($headerTestSubclass, ",") .",";
        $contents .= implode($header2, ",") ."\n";

        //SQL文発行
        $data = false;
        $examno = "";
        $cntScore = array();
        $number = $cntMan = $cntWoman = $cntTotal = 0;
        $arrData1 = $arrData2 = $arrDataScore = array();
        $query = knjl358cQuery::selectCsvQuery($this->ObjYear, $this->field["APPLICANTDIV"], $testdiv, $shdiv, $this->field["SORT"], $this->field["TARGET"], $this->isGojou, $this->field["TESTDIV"], $this->isCollege);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            if ($examno != $row["EXAMNO"]) {
                $examno = $row["EXAMNO"];
                $number++;
                $cntTotal++;
                if ($row["SEX"] == "1") $cntMan++;
                if ($row["SEX"] == "2") $cntWoman++;
                $arrData1[$row["EXAMNO"]] .= $number .",";
                $arrData1[$row["EXAMNO"]] .= $row["EXAMNO"] .",";
                $arrData1[$row["EXAMNO"]] .= ($this->field["OUTPUT"] == "1") ? "," : $row["NAME"] .",";
                $arrData1[$row["EXAMNO"]] .= $row["SEX_NAME"] .",";
                $arrData1[$row["EXAMNO"]] .= $row["PREF_NAME"] .",";
                $arrData1[$row["EXAMNO"]] .= $row["FINSCHOOL_NAME"] .",";
                if ($this->isCollege && $this->field["APPLICANTDIV"] == "1" && $testdiv == "1") {
                    $arrData1[$row["EXAMNO"]] .= $row["SUB_ORDER_NAME"] .",";
                    $arrData2[$row["EXAMNO"]] .= $row["TOTAL2"] .",";
                    $arrData2[$row["EXAMNO"]] .= $row["AVERAGE2"] .",";
                    $arrData2[$row["EXAMNO"]] .= $row["TOTAL3"] .",";
                    $arrData2[$row["EXAMNO"]] .= $row["AVERAGE3"] .",";
                    $arrData2[$row["EXAMNO"]] .= $row["AVERAGE1"] .",";
                    $arrData2[$row["EXAMNO"]] .= $row["RANK1"] .",";
                } else {
                    $arrData2[$row["EXAMNO"]] .= $row["TOTAL"] .",";
                    $arrData2[$row["EXAMNO"]] .= $row["AVERAGE"] .",";
                    $arrData2[$row["EXAMNO"]] .= $row["RANK"] .",";
                }
                $arrData2[$row["EXAMNO"]] .= $row["JUDGENAME"] .",";
                $arrData2[$row["EXAMNO"]] .= $row["RECOM_TESTDIVNAME"] .$row["RECOM_EXAMNO"] .$row["SHIFT_FLG"] .$row["REMARK1"] .$row["REMARK2"] .",";
                if ($this->isCollege && $this->field["APPLICANTDIV"] == "1" && $testdiv == "1") {
                    if ($row["ATTEND_ALL_FLG"] == "1" && strlen($row["TOTAL2"]))    $cntScore["TOTAL2"][]   = $row["TOTAL2"];
                    if ($row["ATTEND_ALL_FLG"] == "1" && strlen($row["AVERAGE2"]))  $cntScore["AVERAGE2"][] = $row["AVERAGE2"];
                    if ($row["ATTEND_ALL_FLG"] == "1" && strlen($row["TOTAL3"]))    $cntScore["TOTAL3"][]   = $row["TOTAL3"];
                    if ($row["ATTEND_ALL_FLG"] == "1" && strlen($row["AVERAGE3"]))  $cntScore["AVERAGE3"][] = $row["AVERAGE3"];
                } else {
                    if ($row["ATTEND_ALL_FLG"] == "1")  $cntScore["TOTAL"][]    = $row["TOTAL"];
                    if ($row["ATTEND_ALL_FLG"] == "1")  $cntScore["AVERAGE"][]  = $row["AVERAGE"];
                }
            }
            $arrDataScore[$row["EXAMNO"]][$row["TESTSUBCLASSCD"]] .= ($row["ATTEND_FLG"] == "1") ? $row["SCORE"] : "*";
            if ($row["ATTEND_FLG"] == "1") $cntScore[$row["TESTSUBCLASSCD"]][] = $row["SCORE"];

            $data = true;
        }
        $result->free();

        //CSV
        foreach ($arrData1 as $key => $val) {
            $contents .= $arrData1[$key];
            foreach ($arrTestSubclass as $code => $name) {
                if (strlen($arrDataScore[$key][$code])) {
                    $contents .= $arrDataScore[$key][$code] .",";
                } else {
                    $contents .= "-" .",";
                }
            }
            $contents .= $arrData2[$key] ."\n";
        }

        if ($this->isCollege && $this->field["APPLICANTDIV"] == "1" && $testdiv == "1") {
            //人数
            $contents .= ",,,,,,人数,";
            foreach ($arrTestSubclass as $code => $name) {
                $contents .= get_count($cntScore[$code]) .",";
            }
            $contents .= get_count($cntScore["TOTAL2"]) ."," .get_count($cntScore["AVERAGE2"]) ."," .get_count($cntScore["TOTAL3"]) ."," .get_count($cntScore["AVERAGE3"]) ."\n";
            //平均
            $contents .= ",,,,,,平均,";
            foreach ($arrTestSubclass as $code => $name) {
                $avg = (0 < get_count($cntScore[$code])) ? round((array_sum($cntScore[$code])/get_count($cntScore[$code]))*10)/10 : "";
                $contents .= $avg .",";
            }
            $avgTotal2   = (0 < get_count($cntScore["TOTAL2"]))   ? round((array_sum($cntScore["TOTAL2"])/get_count($cntScore["TOTAL2"]))*10)/10 : "";
            $avgAverage2 = (0 < get_count($cntScore["AVERAGE2"])) ? round((array_sum($cntScore["AVERAGE2"])/get_count($cntScore["AVERAGE2"]))*10)/10 : "";
            $avgTotal3   = (0 < get_count($cntScore["TOTAL3"]))   ? round((array_sum($cntScore["TOTAL3"])/get_count($cntScore["TOTAL3"]))*10)/10 : "";
            $avgAverage3 = (0 < get_count($cntScore["AVERAGE3"])) ? round((array_sum($cntScore["AVERAGE3"])/get_count($cntScore["AVERAGE3"]))*10)/10 : "";
            $contents .= $avgTotal2 ."," .$avgAverage2 ."," .$avgTotal3 ."," .$avgAverage3 ."\n";
        } else {
            //人数
            $contents .= ",,,,,人数,";
            foreach ($arrTestSubclass as $code => $name) {
                $contents .= get_count($cntScore[$code]) .",";
            }
            $contents .= get_count($cntScore["TOTAL"]) ."," .get_count($cntScore["AVERAGE"]) ."\n";
            //平均
            $contents .= ",,,,,平均,";
            foreach ($arrTestSubclass as $code => $name) {
                $avg = (0 < get_count($cntScore[$code])) ? round((array_sum($cntScore[$code])/get_count($cntScore[$code]))*10)/10 : "";
                $contents .= $avg .",";
            }
            $avgTotal   = (0 < get_count($cntScore["TOTAL"]))   ? round((array_sum($cntScore["TOTAL"])/get_count($cntScore["TOTAL"]))*10)/10 : "";
            $avgAverage = (0 < get_count($cntScore["AVERAGE"])) ? round((array_sum($cntScore["AVERAGE"])/get_count($cntScore["AVERAGE"]))*10)/10 : "";
            $contents .= $avgTotal ."," .$avgAverage ."\n";
        }
        //男女計
        $contents .= "男".$cntMan."名、女".$cntWoman."名、合計".$cntTotal."名" ."\n"."\n";

        Query::dbCheckIn($db);

        return ($data) ? $contents : "";
    }
}
?>
