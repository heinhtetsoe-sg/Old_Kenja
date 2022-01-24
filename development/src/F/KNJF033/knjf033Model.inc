<?php

require_once('for_php7.php');

class knjf033Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ

    function knjf033Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("GRADE"        =>  VARS::post("GRADE"),        //学年
                             "DBNAME"       =>  VARS::post("DBNAME")        //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
        $this->selectdata   = VARS::post("selectdata");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    function getDownloadModel()
    {
        $contents = "";

        //ヘッダ
        $header = array("GRADE"             => "",
                        "HR_CLASS"          => "",
                        "ATTENDNO"          => "",
                        "NAME"              => "",
                        "SEX"               => "",
                        "HEIGHT"            => "",
                        "WEIGHT"            => "",
                        "SITHEIGHT"         => "",
                        "SHINTAIHI"         => "",
                        "NUTRITIONCD"       => "",
                        "R_BAREVISION_MARK" => "裸眼視力",
                        "L_BAREVISION_MARK" => "裸眼視力",
                        "R_VISION_MARK"     => "矯正視力",
                        "L_VISION_MARK"     => "矯正視力",
                        "EYEDISEASECD"      => "眼科",
                        "R_EAR"             => "聴力",
                        "L_EAR"             => "聴力",
                        "TB_REMARKCD"       => "結核検診",
                        "TB_NAMECD"    => "結核検診",
                        "HEART_MEDEXAM"     => "心臓検診",
                        "HEARTDISEASECD"    => "心臓検診",
                        "ALBUMINURIA1CD"    => "尿１次検査",
                        "URICSUGAR1CD"      => "尿１次検査",
                        "URICBLEED1CD"      => "尿１次検査",
                        "ALBUMINURIA2CD"    => "尿２次検査",
                        "URICSUGAR2CD"      => "尿２次検査",
                        "URICBLEED2CD"      => "尿２次検査",
                        "HINKETU"           => "貧血検査",
                        "HINKETU_REMARK"    => "貧血検査",
                        "DOC_CD"            => "",
                        "OTHERDISEASECD"    => ""
                        );

        $header2 = array("GRADE"            => "学年",
                        "HR_CLASS"          => "組",
                        "ATTENDNO"          => "出席番号",
                        "NAME"              => "氏名",
                        "SEX"               => "性別",
                        "HEIGHT"            => "身長",
                        "WEIGHT"            => "体重",
                        "SITHEIGHT"         => "座高",
                        "SHINTAIHI"         => "身体比",
                        "NUTRITIONCD"       => "栄養状態",
                        "R_BAREVISION_MARK" => "右",
                        "L_BAREVISION_MARK" => "左",
                        "R_VISION_MARK"     => "右",
                        "L_VISION_MARK"     => "左",
                        "EYEDISEASECD"      => "検診結果",
                        "R_EAR"             => "右",
                        "L_EAR"             => "左",
                        "TB_REMARKCD"       => "レントゲン",
                        "TB_NAMECD"    => "精密検診",
                        "HEART_MEDEXAM"     => "心電図",
                        "HEARTDISEASECD"    => "精密",
                        "ALBUMINURIA1CD"    => "蛋白",
                        "URICSUGAR1CD"      => "糖",
                        "URICBLEED1CD"      => "潜血",
                        "ALBUMINURIA2CD"    => "蛋白",
                        "URICSUGAR2CD"      => "糖",
                        "URICBLEED2CD"      => "潜血",
                        "HINKETU"           => "g/dl",
                        "HINKETU_REMARK"    => "所見",
                        "DOC_CD"            => "内科検診",
                        "OTHERDISEASECD"    => "その他"
                        );

        $contents .= implode($header, ",") ."\n";
        $contents .= implode($header2, ",") ."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //SQL文発行
        $query = knjf033Query::getSelectCsv($this);
        $result = $db->query($query);
        $data = false;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row["SEX"]             = $db->getOne(knjf033Query::getNameMst('Z002', $row["SEX"]));
            $row["NUTRITIONCD"]     = $db->getOne(knjf033Query::getNameMst('F030', $row["NUTRITIONCD"]));
            $row["EYEDISEASECD"]    = $db->getOne(knjf033Query::getNameMst('F050', $row["EYEDISEASECD"]));
            $row["R_EAR"]           = $db->getOne(knjf033Query::getNameMst('F010', $row["R_EAR"]));
            $row["L_EAR"]           = $db->getOne(knjf033Query::getNameMst('F010', $row["L_EAR"]));
            $row["TB_REMARKCD"]     = $db->getOne(knjf033Query::getNameMst('F100', $row["TB_REMARKCD"]));
            $row["TB_NAMECD"]       = $db->getOne(knjf033Query::getNameMst('F120', $row["TB_NAMECD"]));
            $row["HEART_MEDEXAM"]   = $db->getOne(knjf033Query::getNameMst('F080', $row["HEART_MEDEXAM"]));
            $row["HEARTDISEASECD"]  = $db->getOne(knjf033Query::getNameMst('F090', $row["HEARTDISEASECD"]));
            $row["ALBUMINURIA1CD"]  = $db->getOne(knjf033Query::getNameMst('F020', $row["ALBUMINURIA1CD"]));
            $row["URICSUGAR1CD"]    = $db->getOne(knjf033Query::getNameMst('F019', $row["URICSUGAR1CD"]));
            $row["URICBLEED1CD"]    = $db->getOne(knjf033Query::getNameMst('F018', $row["URICBLEED1CD"]));
            $row["ALBUMINURIA2CD"]  = $db->getOne(knjf033Query::getNameMst('F020', $row["ALBUMINURIA2CD"]));
            $row["URICSUGAR2CD"]    = $db->getOne(knjf033Query::getNameMst('F019', $row["URICSUGAR2CD"]));
            $row["URICBLEED2CD"]    = $db->getOne(knjf033Query::getNameMst('F018', $row["URICBLEED2CD"]));
            $row["DOC_CD"]          = $db->getOne(knjf033Query::getNameMst('F144', $row["DOC_CD"]));
            $row["OTHERDISEASECD"]  = $db->getOne(knjf033Query::getNameMst('F140', $row["OTHERDISEASECD"]));

            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            $contents .= implode($row, ",") ."\n";
            $data = true;
        }

        $result->free();
        Query::dbCheckIn($db);

        if ($data){
            $couse = CTRL_YEAR."年度　健康診断結果ＣＳＶ出力";
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
