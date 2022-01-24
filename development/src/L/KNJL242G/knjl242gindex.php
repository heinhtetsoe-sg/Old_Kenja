<?php

require_once('for_php7.php');

require_once('knjl242gModel.inc');
require_once('knjl242gQuery.inc');

class knjl242gController extends Controller {
    var $ModelClassName = "knjl242gModel";
    var $ProgramID      = "KNJL242G";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl242g":                                //��˥塼���̤⤷����SUBMIT�������
                    $sessionInstance->knjl242gModel();        //����ȥ�����ޥ����θƤӽФ�
                    $this->callView("knjl242gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("̤�б��Υ��������{$sessionInstance->cmd}�Ǥ�"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl242gCtl = new knjl242gController;
//var_dump($_REQUEST);
?>
