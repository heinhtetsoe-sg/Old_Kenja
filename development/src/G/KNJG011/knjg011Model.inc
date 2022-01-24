<?php

require_once('for_php7.php');


class knjg011Model extends Model 
{
    var $field      = array();
    var $cmd;
    var $ctrl = array();
    
    function knjg011Model()
    {        
        common::GetControlMaster_Fnc($this->ctrl);
    }
    
    function init()
    {  
        $this->field = array("GRADE"            =>  VARS::post("GRADE"),            //学年
                             "CERTIF_KINDCD"    =>  VARS::post("CERTIF_KINDCD"),    //証明書種類
                             "CERTIF_NO_MAX"    =>  VARS::post("CERTIF_NO_MAX"),    //MAX発行番号
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd        = VARS::request("cmd");

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
        
    //更新
    function &getInsertModel()
    {
        $this->insert();
        $this->setMessage("MSG201");
    }
    
    function &insert()
    {
        $db = Query::dbCheckOut();

        $max_index = $db->getOne(knjg011Query::getCertifIndex());    //MAX申請番号
        if ($this->Properties["certifNoSyudou"] == "1") {
            $max_no = knjg011Query::getCertifNo($db, $this);    //MAX発行番号(CERTIF_ISSUE_DAT)
        } else {
            $max_no = $this->field["CERTIF_NO_MAX"];            //MAX発行番号(CERTIF_ISSUE_DAT)
        }
        $certif_issue_dat_certif_no_max = knjg011Query::getCertifIssueDatCertifNoMax($db, $this);
        $max_detail_no = $this->field["CERTIF_NO_MAX"];            //MAX発行番号(CERTIF_DETAIL_EACHTYPE_DAT)
        $staffname = $db->getOne(knjg011Query::getStaffData());    //発行者
        $result = knjg011Query::getInsertQuery($db, $this, $max_index, $max_no, $staffname, $max_detail_no, $certif_issue_dat_certif_no_max);

        unset($this->field["CERTIF_NO_MAX"]);   //更新後の発行番号を画面に表示するためにここでクリアする

        Query::dbCheckIn($db);
        return $result;
    }
}
?>
