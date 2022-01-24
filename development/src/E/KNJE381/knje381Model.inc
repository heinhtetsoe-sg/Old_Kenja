<?php

require_once('for_php7.php');

class knje381Model extends Model {
    var $cmd;
    var $control;

    function knje381Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->cmd = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    //CSV出力
    function getDownloadModel() {
        //DB接続
        $db = Query::dbCheckOut();

        $contents    = "";

        $areaCdArr   = array();
        $schCateArr  = array();
        $tokyoMcdArr = array();
        $mCdCntArr   = array();
        $setDataArr  = array();

        //ヘッダ（1行目：タイトル）
        $title = "進路調査票";
        $contents .= $title."\n";

        //地区コード
        $areaCdArr["01"] = 'A区分';
        $areaCdArr["02"] = 'B区分';
        $areaCdArr["99"] = '合計';

        //学校種別
        $query = knje381Query::getSchoolCategoryCd();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $schCateArr[$row["VALUE"]] = $row["LABEL"];
        }

        //東京都中分類
        $befCateCd = "";
        $mcdCnt    = 0;
        $query = knje381Query::getTokyoMcd();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($befCateCd != "" && $befCateCd != $row["SCHOOL_CATEGORY_CD"]) {
                $tokyoMcdArr[$befCateCd.'-99-99'] = '小計';
                $mCdCntArr[$befCateCd]            = $mcdCnt + 1;
                $mcdCnt = 0;
            }
            $tokyoMcdArr[$row["VALUE"]] = $row["LABEL"];
            $befCateCd = $row["SCHOOL_CATEGORY_CD"];
            $mcdCnt++;
        }
        $tokyoMcdArr[$befCateCd.'-99-99'] = '小計';
        $tokyoMcdArr['9-99-99'] = '計';
        $mCdCntArr[$befCateCd]            = $mcdCnt + 1;

        //データ取得
        $data = false;
        $befAreaCd   = '';
        $befCateCd   = '';
        $syokeiCnt   = 0;
        $entTotalCnt = 0;
        $totalCnt    = 0;
        $query = knje381Query::getCsvQuery();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",", ".", str_replace('\r\n', '　', $row));

            //小計セット(学校種別毎)
            if ($befCateCd != "" && $befCateCd != $row["AREACD"].'-'.$row["SCHOOL_CATEGORY_CD"]) {
                $setDataArr[$befCateCd.'-99-99'] = $syokeiCnt;
                $syokeiCnt = 0;
            }

            //計セット（地区コード毎）
            if ($befAreaCd != "" && $befAreaCd != $row["AREACD"]) {
                $setDataArr[$befAreaCd.'-9-99-99'] = $entTotalCnt;
                $entTotalCnt = 0;
            }

            $setKey = $row["AREACD"].'-'.$row["SCHOOL_CATEGORY_CD"].'-'.$row["TOKYO_L_CD"].'-'.$row["TOKYO_M_CD"];
            $setDataArr[$setKey] = $row["M_CNT"];

            $syokeiCnt   += $row["M_CNT"];
            $entTotalCnt += $row["M_CNT"];
            $totalCnt    += ($row["AREACD"] == '99') ? $row["M_CNT"]: 0;
            $befCateCd    = $row["AREACD"].'-'.$row["SCHOOL_CATEGORY_CD"];
            $befAreaCd    = $row["AREACD"];
            $data = true;
        }
        //計セット（地区コード毎）
        $setDataArr[$befCateCd.'-99-99'] = $syokeiCnt;
        //全体計
        $setDataArr['99-9-99-99'] = $totalCnt;

        //データセット
        foreach ($areaCdArr as $areaCd => $areaCdName) {
            //地区コード
            $contents .= $areaCdName."\n";

            //学校種別
            foreach ($schCateArr as $keyS => $schCateName) {
                $contents .= $schCateName.str_repeat(',', $mCdCntArr[$keyS]);
            }
            $contents .= "\n";

            //東京都中分類
            $sepM = '';
            foreach ($tokyoMcdArr as $keyM => $mCdName) {
                $contents .= $sepM.$mCdName;
                $sepM = ',';
            }
            $contents .= "\n";

            //データセット
            $sepD = '';
            foreach ($tokyoMcdArr as $keyM => $mCdName) {
                $dataCnt = ($setDataArr[$areaCd.'-'.$keyM] != "") ? $setDataArr[$areaCd.'-'.$keyM]: 0;
                $contents .= $sepD.$dataCnt;
                $sepD = ',';
            }
            $contents .= "\n";
        }

        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($data) {
            $title = "進路調査票";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
