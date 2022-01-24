<?php

require_once('for_php7.php');

require_once('knjl132fModel.inc');
require_once('knjl132fQuery.inc');

class knjl132fController extends Controller {
    var $ModelClassName = "knjl132fModel";
    var $ProgramID      = "KNJL132F";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl132f":                                //��˥塼���̤⤷����SUBMIT�������
                    $sessionInstance->knjl132fModel();        //����ȥ�����ޥ����θƤӽФ�
                    $this->callView("knjl132fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("̤�б��Υ��������{$sessionInstance->cmd}�Ǥ�"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl132fCtl = new knjl132fController;
//var_dump($_REQUEST);
?>
