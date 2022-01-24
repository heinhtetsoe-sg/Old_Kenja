<?php

require_once('for_php7.php');

class knjf131aModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード

    function knjf131aModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("SCHKIND"  => VARS::post("SCHKIND"),  //校種
                             "SDATE"    => VARS::post("SDATE"),    //集計開始日
                             "EDATE"    => VARS::post("EDATE"),    //集計終了日
                             "DBNAME"   => VARS::post("DBNAME")    //DB変更に備えて
                             );

        $this->field["selectdata"] = explode(",", VARS::post("selectdata"));
        $this->selectdata          = VARS::post("selectdata");

        //生徒項目名切替処理
        $schName = "";
        $db = Query::dbCheckOut();
        //テーブルの有無チェック
        $query = knjf131aQuery::checkTableExist();
        $table_cnt = $db->getOne($query);
        if ($table_cnt > 0 && (($this->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") || $this->Properties["use_prg_schoolkind"] == "1")) {
            //生徒項目名取得
            $schName = $db->getOne(knjf131aQuery::getSchName($this));
        }
        $this->sch_label = (strlen($schName) > 0) ? $schName : '生徒';
        Query::dbCheckIn($db);

        $this->kubunList = array();
        $this->kubunList[] = array('label' => '1:内科',          'value' => '1', 'label2' => '内科');
        $this->kubunList[] = array('label' => '2:外科',          'value' => '2', 'label2' => '外科');
        $this->kubunList[] = array('label' => '3:健康相談',      'value' => '5', 'label2' => '健康相談');
        $this->kubunList[] = array('label' => '4:その他',        'value' => '3', 'label2' => 'その他');
        $this->kubunList[] = array('label' => '5:'.$this->sch_label.'以外',      'value' => '4', 'label2' => $this->sch_label.'以外');
        $this->kubunList[] = array('label' => '9:全て',          'value' => '9', 'label2' => '全て');

        $this->cmd = VARS::request("cmd");
    }

    /**
     * ＣＳＶ出力
     */
    function getDownloadModel() {
        //DB接続
        $db = Query::dbCheckOut();

        //タイトル行
        $wrk_nendo = common::DateConv1(str_replace("-","/",CTRL_DATE),10);
        $title = "曜日別保健室利用人数集計表";
        $wrk_date = common::DateConv1(str_replace("-","/",CTRL_DATE),3);
        $wrk_sdate = common::DateConv1(str_replace("-","/",$this->field["SDATE"]),3);
        $wrk_edate = common::DateConv1(str_replace("-","/",$this->field["EDATE"]),3);

        //学校名称2表示
        $schoolname2 = "";
        if (($this->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") || $this->Properties["use_prg_schoolkind"] == "1") {
            $schoolname2 = $db->getOne(knjf131aQuery::getSchoolName2($this))."　　";
        }

        $contents  = ",". $wrk_nendo ."　　". $schoolname2 . $title .",,,,,作成日：". $wrk_date ."\n";
        $contents .= "利用年月日 : ". $wrk_sdate ." ～ ". $wrk_edate ."\n\n";

        $type_name = array("1" => "内科",
                           "2" => "外科",
                           "5" => "健康相談",
                           "3" => "その他",
                           "4" => $this->sch_label."以外",
                           "9" => "全て");

        $type = explode (",", $this->selectdata);
        for ($i = 0; $i < get_count($type); $i++) {

            //ヘッダの作成//
            $header = array("GRADE"    => "（".$type_name[$type[$i]]."）",
                            "SEX"      => "",
                            "NINZUU1"  => "月",
                            "NINZUU2"  => "火",
                            "NINZUU3"  => "水",
                            "NINZUU4"  => "木",
                            "NINZUU5"  => "金",
                            "NINZUU6"  => "土",
                            "NINZUU7"  => "日",
                            "NINZUU"   => "合計",
                            "REMARK"   => "備考"
                            );

            $contents .= implode($header, ",") ."\n";

            $kei = array();
            $kei_otoko_onna = array();
            $query = knjf131aQuery::getGrade($this);
            $result_gdat = $db->query($query);
            while($row_gdat = $result_gdat->fetchRow(DB_FETCHMODE_ASSOC)) {
                //SQL文発行
                $query = knjf131aQuery::selectCsvQuery($this, $row_gdat["GRADE"], $type[$i]);

                $result = $db->query($query);
                $data = false;
                //週毎の小計
                $kei[$row_gdat["GRADE"]] = array();
                while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                    $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    $contents .= "{$row_gdat["GRADE_NAME1"]},{$row["ABBV1"]},{$row["NINZUU1"]},{$row["NINZUU2"]},{$row["NINZUU3"]},{$row["NINZUU4"]},{$row["NINZUU5"]},{$row["NINZUU6"]},{$row["NINZUU7"]},{$row["NINZUU"]}\n";
                    //週毎の小計を計算
                    $kei[$row_gdat["GRADE"]][1] = $kei[$row_gdat["GRADE"]][1] + (int)$row["NINZUU1"];
                    $kei[$row_gdat["GRADE"]][2] = $kei[$row_gdat["GRADE"]][2] + (int)$row["NINZUU2"];
                    $kei[$row_gdat["GRADE"]][3] = $kei[$row_gdat["GRADE"]][3] + (int)$row["NINZUU3"];
                    $kei[$row_gdat["GRADE"]][4] = $kei[$row_gdat["GRADE"]][4] + (int)$row["NINZUU4"];
                    $kei[$row_gdat["GRADE"]][5] = $kei[$row_gdat["GRADE"]][5] + (int)$row["NINZUU5"];
                    $kei[$row_gdat["GRADE"]][6] = $kei[$row_gdat["GRADE"]][6] + (int)$row["NINZUU6"];
                    $kei[$row_gdat["GRADE"]][7] = $kei[$row_gdat["GRADE"]][7] + (int)$row["NINZUU7"];
                    $kei[$row_gdat["GRADE"]][8] = $kei[$row_gdat["GRADE"]][8] + (int)$row["NINZUU"];

                    $kei_otoko_onna[$row["ABBV1"]][1] = $kei_otoko_onna[$row["ABBV1"]][1] + (int)$row["NINZUU1"];
                    $kei_otoko_onna[$row["ABBV1"]][2] = $kei_otoko_onna[$row["ABBV1"]][2] + (int)$row["NINZUU2"];
                    $kei_otoko_onna[$row["ABBV1"]][3] = $kei_otoko_onna[$row["ABBV1"]][3] + (int)$row["NINZUU3"];
                    $kei_otoko_onna[$row["ABBV1"]][4] = $kei_otoko_onna[$row["ABBV1"]][4] + (int)$row["NINZUU4"];
                    $kei_otoko_onna[$row["ABBV1"]][5] = $kei_otoko_onna[$row["ABBV1"]][5] + (int)$row["NINZUU5"];
                    $kei_otoko_onna[$row["ABBV1"]][6] = $kei_otoko_onna[$row["ABBV1"]][6] + (int)$row["NINZUU6"];
                    $kei_otoko_onna[$row["ABBV1"]][7] = $kei_otoko_onna[$row["ABBV1"]][7] + (int)$row["NINZUU7"];
                    $kei_otoko_onna[$row["ABBV1"]][8] = $kei_otoko_onna[$row["ABBV1"]][8] + (int)$row["NINZUU"];

                    $data = true;
                }
                $result->free();


                //週毎の小計の行を出力
                $contents .= "{$row_gdat["GRADE_NAME1"]},計,";
                $contents .= implode(",", $kei[$row_gdat["GRADE"]]) ."\n";
            }

            //週毎の総合計の行を出力
            $sougou_kei = array();
            $query = knjf131aQuery::getGrade($this);
            $result_gdat = $db->query($query);
            while($row_gdat = $result_gdat->fetchRow(DB_FETCHMODE_ASSOC)) {
                $sougou_kei[1] += $kei[$row_gdat["GRADE"]][1];
                $sougou_kei[2] += $kei[$row_gdat["GRADE"]][2];
                $sougou_kei[3] += $kei[$row_gdat["GRADE"]][3];
                $sougou_kei[4] += $kei[$row_gdat["GRADE"]][4];
                $sougou_kei[5] += $kei[$row_gdat["GRADE"]][5];
                $sougou_kei[6] += $kei[$row_gdat["GRADE"]][6];
                $sougou_kei[7] += $kei[$row_gdat["GRADE"]][7];
                $sougou_kei[8] += $kei[$row_gdat["GRADE"]][8];
            }

            foreach ($kei_otoko_onna as $key => $val) {
                $contents .= "合計,{$key},";
                $contents .= implode(",", $kei_otoko_onna[$key]) ."\n";
            }

            $contents .= "合計,計,";
            $contents .= implode($sougou_kei, ",") ."\n\n";
        }

        Query::dbCheckIn($db);

        if ($data){
            $couse="曜日別保健室利用人数集計表";
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
