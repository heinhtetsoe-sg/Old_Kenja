<?php

require_once('for_php7.php');

class knjf153Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード
    var $year;
    var $schoolName;

    function knjf153Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //職員コード
        $this->year = CTRL_YEAR;
    }

    function init() {
        $this->field = array("DBNAME"  =>  VARS::post("DBNAME")    //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
        
        //学校を判断
        $db = Query::dbCheckOut();
        $this->schoolName = $db->getOne(knjf153Query::getSchoolName($this));
        Query::dbCheckIn($db);

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    function getDownloadModel() {

        $col = array();

        $col[]["REASON_NAME"]    = "";
        $col[]["M04CNT"]         = "4月";
        $col[]["M05CNT"]         = "5月";
        $col[]["M06CNT"]         = "6月";
        $col[]["M07CNT"]         = "7月";
        $col[]["M08CNT"]         = "8月";
        $col[]["M09CNT"]         = "9月";
        $col[]["M10CNT"]         = "10月";
        $col[]["M11CNT"]         = "11月";
        $col[]["M12CNT"]         = "12月";
        $col[]["M01CNT"]         = "1月";
        $col[]["M02CNT"]         = "2月";
        $col[]["M03CNT"]         = "3月";
        $col[]["TOTAL"]          = "合計";
        $col[]["PER"]            = "率";


        //クリア
        $contents = "";
        $contents .= ",,,".$this->schoolName."　".$this->year."年度 保健室利用状況表\n";

        //表タイトル
        $contents .= "症状別来室状況\n";

        $titleSep = "";
        $dataCol = array();
        foreach ($col as $renBan => $fieldVal) {
            foreach ($fieldVal as $field => $val) {
                $contents .= $titleSep.$val;
                $titleSep = ",";
                $dataCol[$field] =  $val;
            }
        }
        $contents .= "\n";

        //症状別来室状況
        $idx = 0;
        $db = Query::dbCheckOut();
        $query = knjf153Query::selectCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            $row["PER"] = floor($row["PER"]);

            //値をセット
            $sep = "";
            $label = array("胃・腹痛","生理痛","頭痛","発熱","風邪症状","気分不快・倦怠感","めまい・脳貧血","鼻出血","その他","小計","きず","打撲","捻挫・突き指","肉離れ・筋肉痛","腰痛","火傷","虫さされ・皮膚炎","骨折・脱臼","その他","小計","歯科疾患・異常","眼科疾患・異常","耳鼻科疾患・異常","小計","相談活動","保健室登校(延べ人数)","総合計","休養者数","1日平均利用者数","在籍者数","授業日数");
            $reasonCd = array("TYPE1_01","TYPE1_02","TYPE1_03","TYPE1_04","TYPE1_05","TYPE1_06","TYPE1_07","TYPE1_08","TYPE1_99","TYPE1_SUB","TYPE2_01","TYPE2_02","TYPE2_03","TYPE2_04","TYPE2_05","TYPE2_06","TYPE2_07","TYPE2_08","TYPE2_99","TYPE2_SUB","TYPE3_01","TYPE3_02","TYPE3_03","TYPE3_SUB","R100","04","R101","R102","R103","R104","R105");

            while($row["REASON_CD"] !== $reasonCd[$idx]) {
                //取得できなかった行の挿入
                if($reasonCd[$idx] == "R104" || $reasonCd[$idx] == "R105"){
                    $contents .= $label[$idx].",,,,,,,,,,,,,0,\n";
                }else{
                    $contents .= $label[$idx].",,,,,,,,,,,,,0,0%\n";
                }
                $idx = $idx + 1;
            }

            foreach ($dataCol as $field => $val) {

                if ($field == "REASON_NAME") {
                    $contents .= $sep.$label[$idx];
                }elseif($field == "PER"){
                    if($row["REASON_CD"] == "R104" || $row["REASON_CD"] == "R105"){
                        $contents .= $sep;
                    }else{
                        $contents .= $sep.(intval($row[$field],0))."%";
                    }
                }else{
                    $contents .= $sep.$row[$field];
                }
                $sep = ",";
            }
            $contents .= "\n";
            $idx = $idx + 1;

            $data = true;
        }

         while($idx !== get_count($reasonCd)) {
            //取得できなかった行の挿入
            if($reasonCd[$idx] == "R104" || $reasonCd[$idx] == "R105"){
                $contents .= $label[$idx].",,,,,,,,,,,,,0,\n";
            }else{
                $contents .= $label[$idx].",,,,,,,,,,,,,0,0%\n";
            }
            $idx = $idx + 1;
        }

        //表タイトル
        $contents .= "\n";
        $contents .= "相談活動内容(延べ)\n";

        //相談活動内容(延べ/実人数) , 保健室登校
        $idx = 0;
        $db = Query::dbCheckOut();
        $query = knjf153Query::selectCsvQuery2($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            //表タイトル
            if($idx == 9){
                $contents .= "\n";
                $contents .= "相談活動内容(実人数)\n";
            } elseif($idx == 18){
                $contents .= "\n";
                $contents .= "保健室登校\n";
            }

            //値をセット
            $sep = "";
            $label  = array("人間関係に関わること","進路に関わること","性に関する事","家庭及び家族生活","学校生活に関すること","神経的症状に関すること","疾病・異常に関すること","その他","計","人間関係に関わること","進路に関わること","性に関する事","家庭及び家族生活","学校生活に関すること","神経的症状に関すること","疾病・異常に関すること","その他","計","延べ人数","実人数");
            $reasonCd = array("T1_01","T1_02","T1_03","T1_04","T1_05","T1_06","T1_07","T1_99","T1_SUB","T2_01","T2_02","T2_03","T2_04","T2_05","T2_06","T2_07","T2_99","T2_SUB","R100","R101");

            while($row["REASON_CD"] !== $reasonCd[$idx]) {
                //取得できなかった行の挿入
                $contents .= $label[$idx].",0,0,0,0,0,0,0,0,0,0,0,0,0,\n";
                $idx = $idx + 1;
            }

            foreach ($dataCol as $field => $val) {

                if ($field == "REASON_NAME") {
                    $contents .= $sep.$label[$idx];
                }elseif($field == "PER"){
                    $contents .= $sep;
                }else{
                    $contents .= $sep.$row[$field];
                }
                $sep = ",";
            }
            $contents .= "\n";
            $idx = $idx + 1;

            $data = true;
        }

        while($idx !== get_count($reasonCd)) {
            //表タイトル
            if($idx == 9){
                $contents .= "\n";
                $contents .= "相談活動内容(実人数)\n";
            } elseif($idx == 18){
                $contents .= "\n";
                $contents .= "保健室登校\n";
            }
            //取得できなかった行の挿入
            $contents .= $label[$idx].",0,0,0,0,0,0,0,0,0,0,0,0,0,\n";
            $idx = $idx + 1;
        }

        $result->free();

        /**************************/
        /*  ファイル名用名称取得  */
        /**************************/
        //ファイル名
        $title = $this->schoolName.'　'.$this->year.'年度 保健室利用状況表';

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($data) {
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
?>
