<?php

require_once('for_php7.php');

class knji100c_14Model extends Model {
    var $cmd;
    var $schregno;                  //学籍番号
    var $item;
    var $selectdata_l;              //対象生徒
    var $selectdata_r;              //書出し項目一覧
    var $mode;                      //生徒区分（在校生・卒業生）

    var $getPrgId;
    var $sendAuth;
    var $auth;

    function knja150Model() {}

    function init() {
        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        if (VARS::get("SEND_selectSchoolKind")) {
            $this->selectSchoolKind = VARS::get("SEND_selectSchoolKind");
        }

        //書き出し項目
        $this->item = array();
        $this->item[]["SCHREGNO"]               = "学籍番号";
        $this->item[]["NAME"]                   = "生徒氏名";
        $this->item[]["GRADE"]                  = "学年";
        $this->item[]["HR_CLASS"]               = "組";
        $this->item[]["ATTENDNO"]               = "出席番号";
        $this->item[]["GO_HOME_GROUP_NO"]       = "災害時帰宅グループ番号";
        $this->item[]["RESPONSIBILITY"]         = "責任者";
        $this->item[]["HOWTOCOMMUTECD"]         = "通学方法コード";
        $this->item[]["UP_DOWN"]                = "上り下り";
        $this->item[]["COMMUTE_HOURS"]          = "通学所要時間（時）";
        $this->item[]["COMMUTE_MINUTES"]        = "通学所要時間（分）";
        for ($i = 1; $i <= 7; $i++) {
            $label = ($i == 1) ? "(自宅)" : (($i == 7) ? "(学校)" : $i-1);
            $this->item[]["JOSYA_".$i]              = (in_array($i, array(1,7)) ? "最寄駅" : "乗車駅").$label;
            $this->item[]["JOSYANAME_".$i]          = (in_array($i, array(1,7)) ? "最寄駅" : "乗車駅")."名".$label;
            $this->item[]["ROSEN_".$i]              = "路線".$label;
            $this->item[]["ROSENNAME_".$i]          = "路線名".$label;
            $this->item[]["GESYA_".$i]              = "下車駅".$label;
            $this->item[]["GESYANAME_".$i]          = "下車駅名".$label;
            $this->item[]["FLG_".$i]                = "通学手段".$label;
        }

        //ITEM
        $this->selectdata_r     = VARS::post("selectdata_r");
        //対象生徒
        $this->selectdata_l     = VARS::post("selectdata_l");
        //学年
        $this->grade            = VARS::post("GRADE");
        //年組
        $this->grade_hr_class   = VARS::post("GRADE_HR_CLASS");
        //異動対象日付
        $this->date             = VARS::post("DATE");
        //出力設定
        $this->output           = VARS::post("OUTPUT");

        if (VARS::get("SEND_PRGID")) {
            unset($this->subsystem);
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }

        $this->getPrgId = VARS::get("SEND_PRGID");
        $this->sendAuth = VARS::get("SEND_AUTH");
        $this->auth     = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        //サブシステム
        if(VARS::get("SUBSYSTEM"))
            $this->subsystem        = VARS::get("SUBSYSTEM");
        //対象生徒
        if(VARS::get("selectdata_l"))
            $this->selectdata_l     = VARS::get("selectdata_l");
        //学年
        if(VARS::get("GRADE"))
            $this->grade            = VARS::get("GRADE");
        //年組
        if(VARS::get("GRADE_HR_CLASS"))
            $this->grade_hr_class   = VARS::get("GRADE_HR_CLASS");
        //出力設定
        if(VARS::get("OUTPUT"))
            $this->output           = VARS::get("OUTPUT");
    }

    //CSV書出し
    function &getCsvModel() {
        $db = Query::dbCheckOut();
        $item = explode(",", $this->selectdata_r);

        //選択フィールド削除
        $query = knji100c_14Query::getDelFieldSql($this);
        $db->query($query);

        //選択フィールド保存
        $setSeq = 1;
        foreach ($item as $key => $val) {
            $query = knji100c_14Query::getFieldInsSql($this, $setSeq, $val);
            $db->query($query);
            $setSeq++;
        }

        //項目名一覧
        $item_array = array();
        foreach($this->item as $key => $val) {
            foreach ($val as $field => $label) {
                $item_array[$field] = $label;
            }
        }

        //駅ネットマスタ取得
        $routeArray = array();
        $result = $db->query(knji100c_14Query::getStationNetMst($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $routeArray[$row["STATION_CD"]] = array("L" => $row["LINE_NAME"], "S" => $row["STATION_NAME"]);
        }
        $result->free();

        $contents = $sep = "";
        //ヘッダ作成
        for ($i = 0; $i < sizeof($item); $i++) {
            $contents .= $sep .$item_array[$item[$i]];
            $sep = ",";
        }
        $contents .= "\n";

        //データ部作成
        $result = $db->query(knji100c_14Query::selectUngrdQuery($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            for ($r = 1; $r <= 7; $r++) {
                $row["JOSYANAME_".$r] = ($row["FLG_".$r] == "1") ? $routeArray[$row["JOSYA_".$r]]["S"] : "";
                $row["ROSENNAME_".$r] = ($row["FLG_".$r] == "1") ? $routeArray[$row["JOSYA_".$r]]["L"] : "";
                $row["GESYANAME_".$r] = ($row["FLG_".$r] == "1") ? $routeArray[$row["GESYA_".$r]]["L"] : "";
            }

            $sep = "";
            for($i = 0; $i < sizeof($item); $i++){
                $contents .= $sep .$row[$item[$i]];
                $sep = ",";
            }
            $contents .= "\n";
            $data = true;
        }
        $result->free();

        Query::dbCheckIn($db);

        if ($data) {
            $filename = sprintf("%s.csv","生徒基礎データ（生徒環境調査）");
            common::downloadContents($contents, $filename);
            exit;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>