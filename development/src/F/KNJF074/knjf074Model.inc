<?php

require_once('for_php7.php');

class knjf074Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード

    function knjf074Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init() {
        $this->field = array("YEAR" =>  VARS::post("YEAR"),  //年度
                             "DBNAME"  =>  VARS::post("DBNAME")    //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    /**
     * ＣＳＶ出力
     */
    function getDownloadModel() {
        //DB接続
        $db = Query::dbCheckOut();
        $yval = $this->field["YEAR"];
        $wrk_nendo = common::Calc_Wareki($yval, "04", "01");
        $ttlstr = $wrk_nendo."度 学校保健統計（疾病異常）集計表";

        //学校名称表示
        $schoolname = "";
        $schoolname = $db->getOne(knjf074Query::getSchoolName($this)).",";

        $contents = $ttlstr.",".$schoolname."\n";

        //ヘッダの作成//
        $header = array();
        $header["SEX"] = "性別";
        if ($this->Properties["useSpecial_Support_School"] == "1") {
            $header["SCHOOL_KIND"] = "校種";
        } else {
            $header["AGE"] = "年齢";
        }
        $header["STU_CNT"] = "生徒数";
        $header["STU_CNT"] = "生徒数";
        $header["YASE_SUM"] = "やせ傾向";
        $header["HIMAN_SUM"] = "肥満傾向　（＋２０％以上）";
        $header["SEKICYU_SOKUWAN_SUM"] = "脊柱側わん症・脊柱側わんの者";
        $header["OTHER_SEKICYU_SOKUWAN_SUM"] = "その他の脊柱異常・胸郭異常";
        $header["SISI_SUM"] = "四肢の状態";
        $header["ATOPY_SKIN_SUM"] = "アトピー性皮膚炎";
        $header["OTHER_SKIN_SUM"] = "その他の皮膚疾患・異常";
        $header["HEART_DIS_SUM"] = "心臓の疾病・異常";
        $header["HEARTBEAT_DIS_SUM"] = "心電図の異常";
        $header["ZENSOKU_SUM"] = "ぜん息";
        $header["JINZOU_SUM"] = "腎臓疾患";
        $header["KENTOU_SUM"] = "委員会での検討を必要とする者";
        $header["KEKKAKU_CHK_DETAIL_SUM"] = "結核の精密検査の対象者";
        $header["KEKKAKU_SUM"] = "結核";
        $header["OTHER_ARELGY_SUM"] = "その他のアレルギー疾患";
        $header["OTHER_DISEASE_SUM"] = "その他の疾病・異常";
        $header["NANCYOU_SUM"] = "難聴";
        $header["EAR_SIPPEI_SUM"] = "耳疾患";
        $header["NOSE_SUB_SIPPEI_SUM"] = "鼻・副鼻腔疾患";
        $header["THROAT_HEAD_SIPPEI_SUM"] = "咽喉頭疾患";
        $header["EYE_SIPPEI_SUM"] = "眼疾患・異常";
        $header["EYECHK_INSPECT_SUM"] = "視力検査受検者数";
        $header["NAKEDEYE_1_OVER_SUM"] = "裸眼視力１．０以上の者";
        $header["B_SUM"] = "視力Ｂ(０．９～０．７)";
        $header["C_SUM"] = "視力Ｃ(０．６～０．３)";
        $header["D_SUM"] = "視力Ｄ(０．３未満)";
        $header["BCD_SUM"] = "合　計";
        $header["EYECHK_OMIT_SUM"] = "裸眼視力検査を省略した者";
        $header["TOOTHCHK_INSPECT_SUM"] = "歯科検診受検者数";
        $header["NO_REMAIN_UBA_TOOTH_SUM"] = "う歯なしの者";
        $header["UBA_TOOTH_HOLDER_SUM"] = "う歯の保有者/処置完了者";
        $header["REMAIN_UBA_TOOTH_SUM"] = "う歯の保有者/未処置者";
        $header["TOTAL_UBA_TOOTH_SUM"] = "う歯の保有者/合計";
        $header["UBA_TOOTH_SUM"] = "永久歯のう歯数等/う歯等数";
        $header["LOSTADULTTOOTH_SUM"] = "永久歯のう歯数等/喪失歯数";
        $header["TOOTH_DMFT_INDEX"] = "永久歯のう歯数等/ＤＭＦＴ指数";
        $header["OTHER_CHIN_JOINT_SUM"] = "その他の歯疾・異常/顎関節";
        $header["OTHER_TOOTH_ALIGNMENT_SUM"] = "その他の歯疾・異常/歯列・咬合";
        $header["PLAQUE_SUM"] = "その他の歯疾・異常/歯垢の状態";
        $header["GUM_SUM"] = "その他の歯疾・異常/歯肉の状態";
        $header["OTHER_TOOTH_SIPPEI_SUM"] = "その他の歯疾・異常/その他の疾病・異常";
        $header["TOOTH_DISEASE_SUM"] = "口腔の疾病・異常";
        $header["URINE_CHK_INSPECT_SUM"] = "尿検査受検者数";
        $header["URINE_CHKSND_PROTEIN_SUM"] = "尿二次検査結果/蛋白検出者";
        $header["URINE_CHKSND_SUGAR_SUM"] = "尿二次検査結果/糖検出者";
        $header["URINE_CHKSND_HIDEBLOOD_SUM"] = "尿二次検査結果/潜血検出者";
        $header["ATOPY_SKIN_SUM2"] = "アトピー性皮膚炎";
        $header["ZENSOKU_SUM2"] = "ぜん息";
        $header["NOSE_ARELGY_SUM"] = "アレルギー性鼻炎";
        $header["EYE_ARELGY_SUM"] = "アレルギー性結膜炎";
        $header["FOOD_ALLERGY_SUM"] = "食物アレルギー";
        $header["HAY_FEVER_SUM"] = "花粉症";
        $header["MEDICINE_ALLERGY_SUM"] = "薬物アレルギー";
        $header["JINMASIN_SUM"] = "じんましん";
        $header["OTHER_ALLERGY_SUM"] = "その他のアレルギー";

        $contents .= implode($header, ",") ."\n";

        //出力チェックリスト作成
        $kchklist = array();
        if ($this->Properties["useSpecial_Support_School"] == "1") {
            //この出力順序で出力。違う場合は、その行のデータを空データで出力。
            $kchklist = array(array(SEX=>"1", SCHOOL_KIND => "K"),
                              array(SEX=>"1", SCHOOL_KIND => "P"),
                              array(SEX=>"1", SCHOOL_KIND => "J"),
                              array(SEX=>"1", SCHOOL_KIND => "H"),
                              array(SEX=>"1", SCHOOL_KIND => "合計"),
                              array(SEX=>"2", SCHOOL_KIND => "K"),
                              array(SEX=>"2", SCHOOL_KIND => "P"),
                              array(SEX=>"2", SCHOOL_KIND => "J"),
                              array(SEX=>"2", SCHOOL_KIND => "H"),
                              array(SEX=>"2", SCHOOL_KIND => "合計"));
        } else {
            //この出力順序で出力。違う場合は、その行のデータを空データで出力。
            $kchklist = array(array(SEX=>"1", SCHOOL_KIND => "15"),
                              array(SEX=>"1", SCHOOL_KIND => "16"),
                              array(SEX=>"1", SCHOOL_KIND => "17"),
                              array(SEX=>"1", SCHOOL_KIND => "合計"),
                              array(SEX=>"2", SCHOOL_KIND => "15"),
                              array(SEX=>"2", SCHOOL_KIND => "16"),
                              array(SEX=>"2", SCHOOL_KIND => "17"),
                              array(SEX=>"2", SCHOOL_KIND => "合計"));
        }

        //出力データの作成//
        //SQL文発行
        $query = knjf074Query::selectCsvQuery($this);
        $result = $db->query($query);
        $cnt = 0;
        $setDataWk = array();
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",$row);//データ内にカンマがあればドットに置換・・・置換する文字は、カンマでなければ何でも良い。
            $setDataWk[] = $row;
        }
        $cntback = $cnt;
        //チェックリストに登録されているデータと突き合わせて、存在しないデータについては空データを埋める。
        while ($cnt < get_count($kchklist)) {
            if ($cnt > 10) break;
            $ldat = $kchklist[$cnt];
            $row = array();
            $kchkflg = false;
            if ($this->Properties["useSpecial_Support_School"] == "1") {
                $kchkstr = $ldat["SEX"]."-".$ldat["SCHOOL_KIND"];
                foreach ($setDataWk as $rowGet) {
                    if ($kchkstr == $rowGet["SEX"]."-".$rowGet["SCHOOL_KIND"]) {
                        $row = $rowGet;
                        $kchkflg = true;
                        break;
                    }
                }
            } else {
                $kchkstr = $ldat["SEX"]."-".$ldat["SCHOOL_KIND"];
                foreach ($setDataWk as $rowGet) {
                    if ($kchkstr == $rowGet["SEX"]."-".$rowGet["AGE"]) {
                        $row = $rowGet;
                        $kchkflg = true;
                        break;
                    }
                }
            }
            if (!$kchkflg) {
                foreach ($header as $key => $val) {
                    //ヘッダ部分だけは設定
                    if ($key == "SEX") {
                        $setData[$key] = $ldat[$key] == 1 ? "男子" : "女子";
                    } else if ($key == "SCHOOL_KIND" && $this->Properties["useSpecial_Support_School"] == "1") {
                        if ($ldat[$key] == "K") {
                            $setData[$key] = "幼稚部";
                        } else if ($ldat[$key] == "P") {
                            $setData[$key] = "小学部";
                        } else if ($ldat[$key] == "J") {
                            $setData[$key] = "中学部";
                        } else if ($ldat[$key] == "H") {
                            $setData[$key] = "高等部";
                        } else {
                            $setData[$key] = $ldat[$key];
                        }
                    } else if ($key == "AGE" && $this->Properties["useSpecial_Support_School"] != "1") {
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
                     if ($key == "SEX") {
                         $setData[$key] = $row[$key] == 1 ? "男子" : "女子";
                     } else if ($key == "SCHOOL_KIND" && $this->Properties["useSpecial_Support_School"] == "1") {
                         if ($row[$key] == "K") {
                             $setData[$key] = "幼稚部";
                         } else if ($row[$key] == "P") {
                             $setData[$key] = "小学部";
                         } else if ($row[$key] == "J") {
                             $setData[$key] = "中学部";
                         } else if ($row[$key] == "H") {
                             $setData[$key] = "高等部";
                         } else {
                             $setData[$key] = $row[$key];
                         }
                     } else if ($key == "AGE" && $this->Properties["useSpecial_Support_School"] != "1") {
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
            $contents .= implode($setData, ",") ."\n";
            $cnt++;
        }

        $result->free();
        Query::dbCheckIn($db);

        if ($data){
            $couse=$ttlstr;
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        }else{
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
