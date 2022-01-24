<?php

require_once('for_php7.php');


class knjm210m_3Model extends Model {

    var $field      = array();
    var $cmd;
    var $GradeClass = "";
    var $schregno;
    var $control_data = array();    //コントロールマスタデータを保持


    function knjm210m_3Model()
    {
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init()
    {
        if(VARS::get("SCHNO")) $this->schregno = VARS::get("SCHNO");

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
    }

    //各コンボボックス作成
    function CreateCombo($objForm,$db,$namecd,$varname,$value,$fst)
    {
        $result = $db->query(knjm210m_3Query::getNameMst_data($namecd));        
        $opt = array();

        //性別と卒業区分には先頭に空をセット
        if ($fst=="1") $opt[] = array("label" => "","value" => "");

        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
              $opt[] = array( "label" => $row["NAMECD2"]."  ".htmlspecialchars($row["NAME1"]),
                              "value" => $row["NAMECD2"]);
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => $varname,
                            "size"        => 1,
                            "maxlength"   => 10,
                            "extrahtml"   => "",
                            "value"       => $value,
                            "options"     => $opt));

        return $objForm->ge($varname);
    }
}
?>
